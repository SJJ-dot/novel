package sjj.fiction

interface BaseView<in T : BasePresenter> {
    fun setPresenter(presenter: T)
}
