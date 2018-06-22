package sjj.fiction.main

import android.arch.lifecycle.LiveDataReactiveStreams
import android.arch.lifecycle.ViewModel
import sjj.fiction.data.repository.fictionDataRepository

class MainViewModel : ViewModel() {
    val books = LiveDataReactiveStreams.fromPublisher(fictionDataRepository.getBooks())
}