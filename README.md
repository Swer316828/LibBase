# LibBase
快速开发3.0.0MVVM 使用
步骤一：在项目根build.gradle中

allprojects {
    repositories {
 
        maven { url 'https://jitpack.io' }
    }
}

ext {
    //依赖包
    libs = [
            android_supportV7: 'com.android.support:appcompat-v7:27.1.0',

            comm_rxjava      : 'io.reactivex.rxjava2:rxjava:2.1.12',
            comm_rxandroid   : 'io.reactivex.rxjava2:rxandroid:2.0.2',

            comm_rft         : 'com.squareup.retrofit2:retrofit:2.4.0',
            comm_rftJson     : 'com.squareup.retrofit2:converter-gson:2.4.0',
            comm_rftAdapter  : 'com.squareup.retrofit2:adapter-rxjava2:2.4.0',
            comm_rftConverter: 'com.squareup.retrofit2:converter-scalars:2.4.0',

            comm_okhttp      : 'com.squareup.okhttp3:okhttp:3.10.0',
            comm_okhttpLog   : 'com.squareup.okhttp3:logging-interceptor:3.10.0',

            comm_gson        : 'com.google.code.gson:gson:2.8.2',
    ]
}

步骤二：在项目module的build.gradle 中添加依赖
 implementation 'com.github.Swer316828:LibBase:3.0.0'
 
步骤三：在使用Activity，Fragment分别需要继承 AbstractLifecycleActivity<T>,AbstractLifecycleFragment<T>.T是需要处理业务逻辑ViewMode类，在activty
 or Fragment 中通过LiveDataMatch 注解方式响应ViewMode中对应数据类型响应到UI 界面上，其中action用于标识是ViewMode中那个方法触发的
 
    @LiveDataMatch(action = "getFileExtMht")
    public void onSuccess(List<File> data) {
        adapter = new ItemAdapter(this, data);
        lv.setAdapter(adapter);
    }
 步骤四：自定义AppContent 需要继承AbstractApplication，需要进行一些初始化操作。AppCacheManager 作为全局缓存类使用，RxBusEventManager用于发送消息通知
 
