package cm.aptoide.ptdev.downloadmanager.state;

import cm.aptoide.ptdev.downloadmanager.DownloadInfo;
import cm.aptoide.ptdev.downloadmanager.DownloadManager;

/**
 * The inactive state represents the status of a download object not doing anything.
 * @author Edward Larsson (edward.larsson@gmx.com)
 */
public class InactiveState extends StatusState {

	/**
	 * Construct an inactive state.
	 * @param downloadInfo The downloadInfo associated with this state.
	 */
	public InactiveState(DownloadInfo downloadInfo) {
		super(downloadInfo);
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
        return EnumState.INACTIVE;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
	public StatusState getShallowCopy() {
		return new InactiveState(null);
	}
}
