package sjj.fiction.util

import sjj.fiction.data.Repository.FictionDataRepository
import sjj.fiction.data.Repository.TestDataRepository
import sjj.fiction.data.Repository.impl.FictionDataRepositoryImpl
import sjj.fiction.data.Repository.impl.TestDataRepositoryIMPL


/**
 * Created by sjj on 2017/8/2.
 */
val fictionDataRepository: FictionDataRepository by lazy { FictionDataRepositoryImpl() }
val testDataRepository: TestDataRepository by lazy { TestDataRepositoryIMPL() }