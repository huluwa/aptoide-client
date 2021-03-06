package cm.aptoide.ptdev.parser;

import android.content.ContentValues;
import android.util.Log;

import com.octo.android.robospice.SpiceManager;
import com.octo.android.robospice.persistence.exception.SpiceException;
import com.octo.android.robospice.request.listener.RequestListener;

import java.io.File;
import java.io.InputStream;
import java.util.Comparator;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import cm.aptoide.ptdev.Aptoide;
import cm.aptoide.ptdev.configuration.AptoideConfiguration;
import cm.aptoide.ptdev.database.Database;
import cm.aptoide.ptdev.database.schema.Schema;
import cm.aptoide.ptdev.model.Login;
import cm.aptoide.ptdev.parser.callbacks.CompleteCallback;
import cm.aptoide.ptdev.parser.callbacks.ErrorCallback;
import cm.aptoide.ptdev.parser.callbacks.PoolEndedCallback;
import cm.aptoide.ptdev.parser.handlers.AbstractHandler;
import cm.aptoide.ptdev.parser.handlers.HandlerInfoXml;
import cm.aptoide.ptdev.services.FileRequest;
import cm.aptoide.ptdev.utils.AptoideUtils;

public class Parser{

    private SpiceManager spiceManager;
    private int i;

    public Parser(SpiceManager manager){
        spiceManager = manager;
        //Log.d("Aptoide-Parser", "newParser");
    }

    public ExecutorService getExecutor() {
        return service;
    }

    PoolEndedCallback poolEndedCallback;

    PriorityBlockingQueue<Runnable> pq = new PriorityBlockingQueue<Runnable>(10, new ComparePriority());

    ExecutorService service = new ThreadPoolExecutor(1, 2, 0L, TimeUnit.MILLISECONDS, pq, new ThreadFactory() {
        @Override
        public Thread newThread(Runnable r) {
            Thread t = new Thread(r);
            t.setPriority(3);
            return t;
        }
    });

    public void parse(final String url, Login login, final int priority, final AbstractHandler handler, final ErrorCallback errorCallback, final CompleteCallback completeCallback, final Runnable runnable) {

        //Log.d("Aptoide-Parser", "Starting parse: " + url);
        int key = url.hashCode();
        AptoideConfiguration configuration = Aptoide.getConfiguration();

        String path = configuration.getPathCache();
        final File file = new File(path+key+".xml");
        i++;
        final long repoId = handler.getRepoId();
        //Log.d("Aptoide-Parser", "Starting request: " + url);
        if(!spiceManager.isStarted()){
            spiceManager.start(Aptoide.getContext());
        }
        spiceManager.execute(new FileRequest(url,file, login), new RequestListener<InputStream>() {
            @Override
            public void onRequestFailure(SpiceException spiceException) {
                //Log.d("Aptoide-Parser", "onRequestFailure");
                i--;
                if(threadPoolIsIdle() && poolEndedCallback!=null){
                    poolEndedCallback.onEnd();
                }
                if(handler instanceof HandlerInfoXml){
                    new Database(Aptoide.getDb()).setFailedRepo(repoId);
                }

                if (errorCallback != null) errorCallback.onError(spiceException, repoId);
            }

            @Override
            public void onRequestSuccess(final InputStream inputStream) {

                //Log.d("Aptoide-Parser", "onRequestSuccess " + url);
                service.execute(new RunnableWithPriority(priority) {
                    @Override
                    public void run() {
                        //Log.d("Aptoide-Parser", "Starting parse " + url);
                        //long startTime = System.currentTimeMillis();
                        Aptoide.getDb().beginTransaction();

                        try {
                            SAXParser parser = SAXParserFactory.newInstance().newSAXParser();
                            //Log.d("Aptoide-Parser", "New SaxParser");
                            if(runnable!=null) 
                                runnable.run();

                            if(handler instanceof HandlerInfoXml){
                                ((HandlerInfoXml)handler).setFile(file);
                                ContentValues values = new ContentValues();
                                values.putNull("is_failed");
                                Aptoide.getDb().update(Schema.Repo.getName(), values, "id_repo = ?", new String[]{String.valueOf(repoId)});
                            }

                            parser.parse(inputStream, handler);
                            //Log.d("Aptoide-Parser", "Parse ended");

                            if (handler instanceof HandlerInfoXml && !((HandlerInfoXml) handler).isDelta() &&
                                    ((HandlerInfoXml) handler).getCountriesPermitted() != null &&
                                    ((HandlerInfoXml) handler).getCountriesPermitted().contains(AptoideUtils.getMyCountry(Aptoide.getContext()))) {
                                RepoLocaleUpdater localeUpdater = new RepoLocaleUpdater(handler.getRepoId(), AptoideUtils.RepoUtils.split(url), Aptoide.getContext(), handler.getDb());
                                localeUpdater.parse();
                            }

                            Aptoide.getDb().setTransactionSuccessful();
                            if(completeCallback!=null)completeCallback.onComplete(repoId);
                        } catch (Exception e1){
                            e1.printStackTrace();
                            if (errorCallback != null) errorCallback.onError(e1, repoId);
                            Log.e("Aptoide-Parser", "Error");
                        }finally {

                            if(Aptoide.getDb().inTransaction()){
                                Aptoide.getDb().endTransaction();
                            }

                        }

                        //Log.d("Aptoide-Parser", "Deleting file");
                        file.delete();

                        i--;
//                        //Log.d("Aptoide-Parser", url + " Took : " + (System.currentTimeMillis() - startTime) + " ms" + " i=" + i);
                        if(threadPoolIsIdle() && poolEndedCallback!=null){
                            poolEndedCallback.onEnd();
                        }

                    }
                });
            }
        });
    }

    private boolean threadPoolIsIdle() {
        return i==0;
    }

    public void parse(String url, Login login, int priority, AbstractHandler handler, Runnable runnable) {
        parse(url, login, priority, handler, null, null, runnable);
    }

    public void setPoolEndCallback(PoolEndedCallback callback) {
        this.poolEndedCallback = callback;
    }

    private class ComparePriority<T extends RunnableWithPriority> implements Comparator<T> {
        @Override
        public int compare(T o1, T o2) {
            return o1.getPriority().compareTo(o2.getPriority());
        }
    }
}