package sjj.fiction.util

import sjj.fiction.data.Repository.FictionDataRepository
import sjj.fiction.data.Repository.impl.FictionDataRepositoryImpl


/**
 * Created by sjj on 2017/8/2.
 */
val fictionDataRepository: FictionDataRepository by lazy { FictionDataRepositoryImpl() }