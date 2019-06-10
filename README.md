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
       implementation 'com.github.Swer316828:LibBase:3.5.15'
    }

   说明：项目已引入框架如下：

     implementation: 'com.android.support:appcompat-v7:27.1.1',
     implementation  'io.reactivex.rxjava2:rxjava:2.2.8',
     implementation  'io.reactivex.rxjava2:rxandroid:2.1.1',

     implementation  'com.squareup.okhttp3:okhttp:3.14.0',
     implementation  'com.squareup.okhttp3:logging-interceptor:3.14.0',
     implementation  'com.google.code.gson:gson:2.8.5',


步骤二：MVVM 框架的使用
3.1 全局唯一Application 必须继承AbstractApplication，必须调用init()方法进行初始化操作。AppCacheManager 作为全局缓存类使用，永久保存与临时存储。

3.2 在Activity，Fragment 中存在业务需求分别对应继承 AbstractLifecycleActivity<T>,AbstractLifecycleFragment<T>，T是需要处理业务逻辑ViewModel类。
业务ViewModel需继承BaseViewModel。

3.3 在activty, Fragment 中通过使用LiveDataMatch关键字进行业务方法绑定设置，使方法响应业务层（ViewMode）中数据回调。其中tag用于说明是ViewMode中那个方法触发的进行说明方便以后查询。

    @LiveDataMatch(tag = "任务说明")
    public void onSuccess(List<File> data) {
        //TODO 数据
    }

    特别说明：同一个UI 层，同一方法名的方法只能存在一个。（方法名作为响应业务回调处理依据）

3.4 RxBusEvent关键字进行消息方法绑定设置，使方法响应消息传递功能。可以在AbstractLifecycleActivity，AbstractLifecycleFragment，BaseViewModel(需开启eventOnOff())子类中使用。

    @RxBusEvent (from = "消息接受说明")
    public void onSuccess(User data) {
        //TODO 数据
    }

    特别说明：建议消息监听放在AbstractLifecycleActivity or AbstractLifecycleFragment中，消息机制独立存在。

3.5 在AbstractLifecycleActivity，AbstractLifecycleFragment 中获取自身关联业务ViewModel调用getViewModel()，使用其他业务ViewModel，调用getViewModel(Class<T> cls)即可

3.6 在BaseViewModel 子类的通过setValue方法，使其UI层进行数据显示刷新。

    3.6.1 setValue(String action)
            参数说明 action:UI层响应方法名

    3.6.2 setValue(String action, Object... data)
            参数说明 action:UI层响应方法名
                     data：响应方法的参数（响应方法的参数顺序一一对应）

    特别说明：UI层业务方法是有参数的，调用 setValue(String action)时，对参数进行补齐处理（对象默认null,基本参数：正常默认值）

3.7 注意点
    3.7.1 在ViewPage 与 AbstractLifecycleFragment 组合使用时：需要注意适配器必须使用FragmentPagerAdapter,因ViewModel，Fragment生命周期关联，Fragment中onDestroyView 被调用，导致ViewModel 进入休眠状态而丢失监听。
    3.7.2 ViewPage 与 AbstractLifecycleFragment 组合使用，另一种解决方式：在调用业务时，唤当前Fragment中ViewModel生命状态。 激活方法：1:this.activateLifecycleEvent(); 指定激活生命周期：this.handleLifecycleEvent(Lifecycle.Event.ON_RESUME);
