package sjj.fiction;

public interface BaseView<T extends BasePresenter> {

    void setPresenter(T presenter);

}
