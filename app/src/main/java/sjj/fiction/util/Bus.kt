package sjj.fiction.util

import io.reactivex.processors.PublishProcessor
import sjj.fiction.model.Event

/**
 * Created by SJJ on 2017/11/25.
 */
val bus = PublishProcessor.create<Event>()