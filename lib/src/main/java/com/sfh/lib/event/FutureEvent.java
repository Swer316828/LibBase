package com.sfh.lib.event;

import java.util.concurrent.Future;

public interface FutureEvent extends Future {

    boolean cancel();

    boolean isCancelled();
}
