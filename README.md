快速开发框架之MVVM设计模式，框架中使用一些主流第三方框架okHttp3，rxjava2，gson,google的MVVM库

步骤一
1.1 在项目根build.gradle中
allprojects {
    repositories {
        maven { url 'https://jitpack.io' }
    }
}

1.2 项目(Module:app)的build.gradle 中添加依赖
    dependencies {
       implementation: 'com.android.support:appcompat-v7:27.1.1',
       implementation  'io.reactivex.rxjava2:rxjava:2.2.8',
       implementation  'io.reactivex.rxjava2:rxandroid:2.1.1',

       implementation  'com.squareup.okhttp3:okhttp:3.14.0',
       implementation  'com.squareup.okhttp3:logging-interceptor:3.14.0',
       implementation  'com.google.code.gson:gson:2.8.5',
       implementation 'com.github.Swer316828:LibBase:xxxx'
    }


步骤二：MVVM 框架的使用
3.1 全局唯一Application 必须继承AbstractApplication，调用init()方法一些初始化操作。AppCacheManager 作为全局缓存类使用，永久保存与临时存储。

3.2 在Activity，Fragment，View 中存在业务需求分别对应继承 AbstractLifecycleActivity<T>,AbstractLifecycleFragment<T>,AbstractLifecycleView<T>。T是需要处理业务逻辑ViewModel类。
业务ViewModel需继承BaseViewModel。

3.3 在activty, Fragment,View 中通过使用LiveDataMatch关键字进行方法注解，使方法响应业务层（ViewMode）中数据回调。其中tag用于说明是ViewMode中那个方法触发的进行说明方便以后查询。

    @LiveDataMatch(tag = "任务说明")
    public void onSuccess(List<File> data) {
        //TODO 数据
    }

3.4 RxBusEvent关键字进行方法注解，使方法响应消息传递功能。可以在BaseViewModel，AbstractLifecycleActivity，AbstractLifecycleFragment，AbstractLifecycleView 的子类中使用。

    @RxBusEvent (from = "消息接受说明")
    public void onSuccess(User data) {
        //TODO 数据
    }
3.5 在AbstractLifecycleActivity，AbstractLifecycleFragment，AbstractLifecycleView 中获取自身关联业务ViewModel调用getViewModel()，使用其他业务ViewModel，调用getViewModel(Class<T> cls)即可

3.6 在BaseViewModel 子类的通过setValue(String action, Object... data)方法，使其UI层进行数据显示刷新。
    参数说明 action:UI层响应方法名，data：响应方法的参数（响应方法的参数顺序一一对应）
 
