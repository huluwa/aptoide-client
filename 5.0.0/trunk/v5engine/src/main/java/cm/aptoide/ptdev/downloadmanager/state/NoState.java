package cm.aptoide.ptdev.downloadmanager.state;

import cm.aptoide.ptdev.downloadmanager.DownloadInfo;
import cm.aptoide.ptdev.downloadmanager.DownloadManager;

/**
 * Created with IntelliJ IDEA.
 * User: rmateus
 * Date: 09-07-2013
 * Time: 11:44
 * To change this template use File | Settings | File Templates.
 */
public class NoState extends StatusState {
    /**
     * Construct a status state.
     *
     * @param downloadObject The downloadObject associated with this state.
     */
    public NoState(DownloadInfo downloadObject) {
        super(downloadObject);
    }

    @Override
    public void download() {
        mDownloadInfo.changeStatusState(new ActiveState(mDownloadInfo));
    }

    @Override
    public void changeFrom() {
        manager.removeFromInactiveList(mDownloadInfo);
    }

    @Override
    public boolean changeTo() {
        if (manager.addToInactiveList(mDownloadInfo)) {
            mDownloadInfo.setStatusState(this);
            return true;
        }

        return false;
    }

    @Override
    public void pause() {
        //do nothing, already inactive
    }

    @Override
    public int getQueuePosition() {
        return Integer.MAX_VALUE;
    }

    @Override
    public EnumState getEnumState() {
        return EnumState.NOSTATE;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public StatusState getShallowCopy() {
        return new InactiveState(null);
    }
}
