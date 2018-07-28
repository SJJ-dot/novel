package sjj.fiction.main

import android.annotation.SuppressLint
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import androidx.navigation.Navigation
import androidx.navigation.ui.NavigationUI
import kotlinx.android.synthetic.main.activity_main.*
import retrofit2.http.Url
import sjj.fiction.BaseActivity
import sjj.fiction.R
import sjj.fiction.util.log
import java.net.URL


class MainActivity : BaseActivity() {
    val url = "https://m.baidu.com/tcx?appui=alaxs&data={%22fromaction%22:%22aladdintrans%22}&page=detail&gid=4193758445&sign=48cda940be5b2e8bdc174dd1177cc378&ts=1532743071&sourceurl=http://www.juyit.com/files/article/html/57/57626/index.html"
    @SuppressLint("CheckResult")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        NavigationUI.setupWithNavController(nav_ui, Navigation.findNavController(this, R.id.nav_host_fragment_main))
        // val  clipboardManager=getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val ul = URL(url)
        ul.authority.log()
        ul.defaultPort.log()
        ul.file.log()
        ul.host.log()
        ul.path.log()
        ul.protocol.log()
        ul.query.log()
        ul.ref.log()
        ul.userInfo.log()
        //if (clipboardManager.hasPrimaryClip()) {
        // val item = clipboardManager.primaryClip.getItemAt(0)
//            item.text ==
        //}
    }
}
