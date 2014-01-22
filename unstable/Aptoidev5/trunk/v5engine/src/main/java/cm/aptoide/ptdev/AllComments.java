package cm.aptoide.ptdev;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.MenuItem;
import cm.aptoide.ptdev.events.BusProvider;
import cm.aptoide.ptdev.services.HttpClientSpiceService;
import com.octo.android.robospice.Jackson2GoogleHttpClientSpiceService;
import com.octo.android.robospice.SpiceManager;

/**
 * Created by rmateus on 26-12-2013.
 */
public class AllComments extends ActionBarActivity {

    private SpiceManager spiceManager = new SpiceManager(HttpClientSpiceService.class);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Aptoide.getThemePicker().setAptoideTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.all_comments);


        getSupportActionBar().setTitle(getString(R.string.comment_see_all));
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    public SpiceManager getSpice() {
        return spiceManager;
    }

    @Override
    protected void onStart() {
        super.onStart();
        spiceManager.start(this);
    }


    @Override
    protected void onStop() {
        super.onStop();
        spiceManager.shouldStop();
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int i = item.getItemId();

        if (i == android.R.id.home) {
            finish();
        } else if (i == R.id.home) {
            finish();
        }

        return super.onOptionsItemSelected(item);
    }
}