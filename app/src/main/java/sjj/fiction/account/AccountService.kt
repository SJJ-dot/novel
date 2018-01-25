package sjj.fiction.account

import android.accounts.AbstractAccountAuthenticator
import android.accounts.Account
import android.accounts.AccountAuthenticatorResponse
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.IBinder

class AccountService : Service() {

    override fun onBind(intent: Intent): IBinder? {
        // TODO: Return the communication channel to the service.
        throw UnsupportedOperationException("Not yet implemented")
    }

    class Authenticator(ctx: Context) : AbstractAccountAuthenticator(ctx) {
        override fun getAuthTokenLabel(authTokenType: String?): String {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        override fun confirmCredentials(response: AccountAuthenticatorResponse?, account: Account?, options: Bundle?): Bundle {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        override fun updateCredentials(response: AccountAuthenticatorResponse?, account: Account?, authTokenType: String?, options: Bundle?): Bundle {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        override fun getAuthToken(response: AccountAuthenticatorResponse?, account: Account?, authTokenType: String?, options: Bundle?): Bundle {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        override fun hasFeatures(response: AccountAuthenticatorResponse?, account: Account?, features: Array<out String>?): Bundle {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        override fun editProperties(response: AccountAuthenticatorResponse?, accountType: String?): Bundle {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        override fun addAccount(response: AccountAuthenticatorResponse?, accountType: String?, authTokenType: String?, requiredFeatures: Array<out String>?, options: Bundle?): Bundle {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

    }
}
