package cm.aptoide.ptdev;

import android.accounts.*;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;

import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;

import static cm.aptoide.ptdev.configuration.AccountGeneral.*;
import static android.accounts.AccountManager.KEY_BOOLEAN_RESULT;

import android.widget.Toast;
import cm.aptoide.ptdev.configuration.AccountGeneral;
import cm.aptoide.ptdev.services.RabbitMqService;
import cm.aptoide.ptdev.utils.Configs;
import com.facebook.Session;
import com.facebook.TokenCachingStrategy;

/**
 * Created by brutus on 09-12-2013.
 */
public class AccountAuthenticator extends AbstractAccountAuthenticator {

    private String TAG = "UdinicAuthenticator";
    private final Context mContext;

    public AccountAuthenticator(Context context) {
        super(context);

        // I hate you! Google - set mContext as protected!
        this.mContext = context;
    }

    @Override
    public Bundle addAccount(AccountAuthenticatorResponse response, String accountType, String authTokenType, String[] requiredFeatures, Bundle options) throws NetworkErrorException {
        Log.d("udinic", TAG + "> addAccount");

        final Intent intent = new Intent(mContext, LoginActivity.class);
        intent.putExtra(LoginActivity.ARG_ACCOUNT_TYPE, accountType);
        intent.putExtra(LoginActivity.ARG_AUTH_TYPE, authTokenType);
        intent.putExtra(LoginActivity.ARG_IS_ADDING_NEW_ACCOUNT, true);
        intent.putExtra(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE, response);

        final Bundle bundle = new Bundle();
        bundle.putParcelable(AccountManager.KEY_INTENT, intent);
        return bundle;
    }

    @Override
    public Bundle getAuthToken(AccountAuthenticatorResponse response, Account account, String authTokenType, Bundle options) throws NetworkErrorException {

        Log.d("udinic", TAG + "> getAuthToken");

        // If the caller requested an authToken type we don't support, then
        // return an error
        if (!authTokenType.equals(AccountGeneral.AUTHTOKEN_TYPE_READ_ONLY) && !authTokenType.equals(AccountGeneral.AUTHTOKEN_TYPE_FULL_ACCESS)) {
            final Bundle result = new Bundle();
            result.putString(AccountManager.KEY_ERROR_MESSAGE, "invalid authTokenType");
            return result;
        }

        // Extract the username and password from the Account Manager, and ask
        // the server for an appropriate AuthToken.
        final AccountManager am = AccountManager.get(mContext);

        String authToken = am.peekAuthToken(account, authTokenType);

        Log.d("udinic", TAG + "> peekAuthToken returned - " + authToken);

        // Lets give another try to authenticate the user
        if (TextUtils.isEmpty(authToken)) {
            final String password = am.getPassword(account);
            if (password != null) {
                try {
                    Log.d("udinic", TAG + "> re-authenticating with the existing password");
                    //authToken = sServerAuthenticate.userSignIn(account.name, password, authTokenType);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        // If we get an authToken - we return it
        if (!TextUtils.isEmpty(authToken)) {
            final Bundle result = new Bundle();
            result.putString(AccountManager.KEY_ACCOUNT_NAME, account.name);
            result.putString(AccountManager.KEY_ACCOUNT_TYPE, account.type);
            result.putString(AccountManager.KEY_AUTHTOKEN, authToken);
            return result;
        }

        // If we get here, then we couldn't access the user's password - so we
        // need to re-prompt them for their credentials. We do that by creating
        // an intent to display our AuthenticatorActivity.
        final Intent intent = new Intent(mContext, LoginActivity.class);
        intent.putExtra(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE, response);
        intent.putExtra(LoginActivity.ARG_ACCOUNT_TYPE, account.type);
        intent.putExtra(LoginActivity.ARG_AUTH_TYPE, authTokenType);
        intent.putExtra(LoginActivity.ARG_ACCOUNT_NAME, account.name);
        final Bundle bundle = new Bundle();
        bundle.putParcelable(AccountManager.KEY_INTENT, intent);
        return bundle;
    }


    @Override
    public String getAuthTokenLabel(String authTokenType) {
        if (AUTHTOKEN_TYPE_FULL_ACCESS.equals(authTokenType))
            return AUTHTOKEN_TYPE_FULL_ACCESS_LABEL;
        else if (AUTHTOKEN_TYPE_READ_ONLY.equals(authTokenType))
            return AUTHTOKEN_TYPE_READ_ONLY_LABEL;
        else
            return authTokenType + " (Label)";
    }

    @Override
    public Bundle hasFeatures(AccountAuthenticatorResponse response, Account account, String[] features) throws NetworkErrorException {
        final Bundle result = new Bundle();
        result.putBoolean(KEY_BOOLEAN_RESULT, false);
        return result;
    }

    @Override
    public Bundle editProperties(AccountAuthenticatorResponse response, String accountType) {
        return null;
    }

    @Override
    public Bundle confirmCredentials(AccountAuthenticatorResponse response, Account account, Bundle options) throws NetworkErrorException {
        return null;
    }

    @Override
    public Bundle updateCredentials(AccountAuthenticatorResponse response, Account account, String authTokenType, Bundle options) throws NetworkErrorException {
        return null;
    }

    @Override
    public Bundle getAccountRemovalAllowed(AccountAuthenticatorResponse response, Account account) throws NetworkErrorException {

        final Bundle result = new Bundle();
        result.putBoolean(AccountManager.KEY_BOOLEAN_RESULT, true);

        if (Build.VERSION.SDK_INT >= 8) {
            Session session = new Session(mContext);
            Session.setActiveSession(session);
            if (Session.getActiveSession() != null) {
                Session.getActiveSession().closeAndClearTokenInformation();
            }
        }
        SharedPreferences sPref = PreferenceManager.getDefaultSharedPreferences(mContext);
        sPref.edit().remove("queueName").commit();
        sPref.edit().remove(Configs.LOGIN_USER_LOGIN).commit();
        sPref.edit().remove("username").commit();
        sPref.edit().remove("useravatar").commit();
        mContext.stopService(new Intent(mContext, RabbitMqService.class));

        return result;

    }
}
