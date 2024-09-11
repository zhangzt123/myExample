package org.example.cache.caffeine;

import com.github.benmanes.caffeine.cache.*;
import com.github.benmanes.caffeine.cache.stats.CacheStats;
import com.github.benmanes.caffeine.cache.stats.StatsCounter;
import org.checkerframework.checker.index.qual.NonNegative;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.UUID;
import java.util.concurrent.*;
import java.util.function.Function;

/**
 *
 */
public class CaffeineExample {

    //W
        /*
            Caffeine 四种缓存
            Cache 原子的k-v 缓存
            AsyncCache 异步的 k-v 缓存
            LoadingCache 自动加载的 k-v缓存
            AsyncLoadingCache 异步的自动加载的 k-v缓存
        */

    public static void main(String[] args) {
        //writeReadCache();
        //writeReadAsyncCache();
        //writeReadLoadingCache();
        writeReadAsyncLoadingCache();
    }

    public static void writeReadCache(){
        try {
        Cache<String, String> buildCache = Caffeine.newBuilder()
                /*缓存条数限制*/
                .maximumSize(1)
                /*缓存项在创建后一段时间内没有被写入，应自动从缓存中删除这个缓存项*/
                .expireAfterWrite(10, TimeUnit.SECONDS)
                /*缓存项在创建后一段时间内若没有被写入或者读取，应自动从缓存中删除这个缓存项*/
                .expireAfterAccess(10, TimeUnit.SECONDS)
                /*指定缓存数据在创建/更新/读取后延长或缩短数据的过期时间 不可与 expireAfterWrite expireAfterAccess 同时使用*/
//                .expireAfter(new Expiry<String, String>() {
//                    @Override
//                    public long expireAfterCreate(@NonNull String key, @NonNull String value, long currentTime) {
//                        if("key".equals(key)){
//                            return 60*1000;
//                        }
//                        return 0;
//                    }
//
//                    @Override
//                    public long expireAfterUpdate(@NonNull String key, @NonNull String value, long currentTime, @NonNegative long currentDuration) {
//                        return 120*1000;
//                    }
//
//                    @Override
//                    public long expireAfterRead(@NonNull String key, @NonNull String value, long currentTime, @NonNegative long currentDuration) {
//                        if("key".equals(key)){
//                            return 60*1000;
//                        }
//                        return 0;
//                    }
//                })
                //.expireAfterWrite()
                /*缓存权重配置 不可与maximumSize同时使用 配合Weigher接口可以自定义缓存键值的权重值的实现*/
                //.maximumWeight(100)
                //.weigher(Weigher.singletonWeigher())
                /*任何移除缓存操作后的监听器*/
                .removalListener((k,v,cause)->{
                    System.out.println(String.format("已删除 k ：%s v ：%s  case：%s",k,v,cause.toString()));
                })
                /*逐出缓存（自动失效）的原子操作期间的监听器*/
                .evictionListener((k,v,cause)->{
                    System.out.println(String.format("删除中 k ：%s v ：%s  case：%s",k,v,cause.toString()));
                })
                /*初始化本地内存占用的大小 以减少扩容时的消耗*/
                .initialCapacity(100)
                /*启用 的 CacheStats 汇总计算 （具有一定的性能消耗）*/
                .recordStats()
                //.recordStats(()-> StatsCounter.disabledStatsCounter())
                /*软引用 虚引用 使用注意 可能会被jvm回收*/
//                .softValues()
//                .weakKeys()
//                .weakValues()
                /*经过指定时间后刷新缓存数据  必须是LoadingCache类型缓存*/
                //.refreshAfterWrite(1,TimeUnit.HOURS)
                /*指定纳秒精度的时间源，用于确定何时应过期或刷新条目。默认情况下， System. nanoTime 使用。
                    此方法的主要目的是便于测试已配置了 expireAfterWrite、 、 expireAfterAccess或 refreshAfterWrite的缓存。*/
                //.ticker(Ticker.systemTicker())
                /*指定异步的线程池*/
                //.executor()
                /*指定计划任务没有则禁用任务*/
                //.scheduler(Scheduler.systemScheduler())
                .build();
        buildCache.put("key", UUID.randomUUID().toString());
        //get方法第二个参数不是null时会返回结果并写入缓存中
        //buildCache.get("key",Function.identity());
        String test1 = buildCache.getIfPresent("key");
        System.out.println("test1:"+test1);
        //移除缓存
        buildCache.invalidate("key");
        String test2 = buildCache.getIfPresent("key");
        System.out.println("test2:"+test2);
        System.out.println(buildCache.stats().toString());
        /*自动失效*/
        buildCache.put("key", UUID.randomUUID().toString());
        String test3 = buildCache.getIfPresent("key");
        System.out.println("test3:"+test3);
        Thread.sleep(15*1000);
        String test4 = buildCache.getIfPresent("key");
        System.out.println("test4:"+test4);
        System.out.println(buildCache.stats().toString());
        Thread.sleep(5*1000);
        } catch (InterruptedException e) {
            //nothing to do
        }
    }

    public static void writeReadAsyncCache(){
        try {
            ForkJoinPool forkJoinPool = new ForkJoinPool(5);
            AsyncCache<String, String> buildCache = Caffeine.newBuilder()
                    /*缓存条数限制*/
                    .maximumSize(1)
                    /*缓存项在创建后一段时间内没有被写入，应自动从缓存中删除这个缓存项*/
                    //.expireAfterWrite(20, TimeUnit.SECONDS)
                    /*缓存项在创建后一段时间内若没有被写入或者读取，应自动从缓存中删除这个缓存项*/
                    .expireAfterAccess(30, TimeUnit.SECONDS)
                    /*指定缓存数据在创建/更新/读取后延长或缩短数据的过期时间 不可与 expireAfterWrite expireAfterAccess 同时使用*/
                    /*.expireAfter(new Expiry<String, String>() {
                    @Override
                    public long expireAfterCreate(@NonNull String key, @NonNull String value, long currentTime) {
                        if("key".equals(key)){
                            return 60*1000;
                        }
                        return 0;
                    }

                    @Override
                    public long expireAfterUpdate(@NonNull String key, @NonNull String value, long currentTime, @NonNegative long currentDuration) {
                        return 120*1000;
                    }

                    @Override
                    public long expireAfterRead(@NonNull String key, @NonNull String value, long currentTime, @NonNegative long currentDuration) {
                        if("key".equals(key)){
                            return 60*1000;
                        }
                        return 0;
                    }
                })*/
                    //.expireAfterWrite()
                    /*缓存权重配置 不可与maximumSize同时使用 配合Weigher接口可以自定义缓存键值的权重值的实现*/
                    //.maximumWeight(100)
                    //.weigher(Weigher.singletonWeigher())
                    /*任何移除缓存操作后的监听器*/
                    .removalListener((k,v,cause)->{
                        System.out.println(String.format("已删除 k ：%s v ：%s  case：%s thread %s ",k,v,cause.toString(),Thread.currentThread().getName()));
                    })
                    /*逐出缓存（自动失效）的原子操作期间的监听器*/
                    .evictionListener((k,v,cause)->{
                        System.out.println(String.format(" 删除中 k ：%s v ：%s  case：%s thread %s ",k,v,cause.toString(),Thread.currentThread().getName()));
                    })
                    /*初始化容量（参考hashmap的initialCapacity） 以减少扩容时的消耗*/
                    .initialCapacity(16)
                    /*启用 的 CacheStats 汇总计算 （具有一定的性能消耗）*/
                    .recordStats()
                    //.recordStats(()-> StatsCounter.disabledStatsCounter())
                    /*软引用 虚引用 使用注意 可能会被jvm回收*/
                    //.softValues()
                    //.weakKeys()
                    //.weakValues()
                    .executor(forkJoinPool)
                    .scheduler(Scheduler.systemScheduler())
                    .buildAsync();
            System.out.println("call writeReadLoadingCache "+ Thread.currentThread().getName());
            CompletableFuture<String> test1 = buildCache.getIfPresent("key");
            System.out.println(String.format("test1 %s" ,test1));
            CompletableFuture<String> test2 = buildCache.get("key", (key, pool) -> {
                CompletableFuture<String> voidCompletableFuture = CompletableFuture.supplyAsync(() -> {
                    String string = UUID.randomUUID().toString();
                    System.out.println(String.format("get create v %s thread %s",string,Thread.currentThread().getName()));
                    return string;
                }, pool);
                return voidCompletableFuture;
            });
            buildCache.put("key",CompletableFuture.supplyAsync(() -> {
                String string = UUID.randomUUID().toString();
                System.out.println(String.format("put create v %s thread %s",string,Thread.currentThread().getName()));
                return string;
            }, forkJoinPool));
            CompletableFuture<String> test3= buildCache.getIfPresent("key");

            //do somthing
            Thread.sleep(10*1000);
            CompletableFuture.allOf(test2,test3).join();
            System.out.println(String.format("test2 %s" ,test2.get()));
            System.out.println(String.format("test3 %s" ,test3.get()));

            Thread.sleep(5*1000);
            forkJoinPool.shutdown();
        } catch (InterruptedException | ExecutionException e) {
            //nothing to do
        }
    }

    public static void writeReadLoadingCache(){
        try {
            LoadingCache<String, String> buildCache = Caffeine.newBuilder()
                    /*缓存条数限制*/
                    .maximumSize(1)
                    /*缓存项在创建后一段时间内没有被写入，应自动从缓存中删除这个缓存项*/
                    //.expireAfterWrite(20, TimeUnit.SECONDS)
                    /*缓存项在创建后一段时间内若没有被写入或者读取，应自动从缓存中删除这个缓存项*/
                    .expireAfterAccess(30, TimeUnit.SECONDS)
                    /*指定缓存数据在创建/更新/读取后延长或缩短数据的过期时间 不可与 expireAfterWrite expireAfterAccess 同时使用*/
                    /*.expireAfter(new Expiry<String, String>() {
                    @Override
                    public long expireAfterCreate(@NonNull String key, @NonNull String value, long currentTime) {
                        if("key".equals(key)){
                            return 60*1000;
                        }
                        return 0;
                    }

                    @Override
                    public long expireAfterUpdate(@NonNull String key, @NonNull String value, long currentTime, @NonNegative long currentDuration) {
                        return 120*1000;
                    }

                    @Override
                    public long expireAfterRead(@NonNull String key, @NonNull String value, long currentTime, @NonNegative long currentDuration) {
                        if("key".equals(key)){
                            return 60*1000;
                        }
                        return 0;
                    }
                })*/
                    //.expireAfterWrite()
                    /*缓存权重配置 不可与maximumSize同时使用 配合Weigher接口可以自定义缓存键值的权重值的实现*/
                    //.maximumWeight(100)
                    //.weigher(Weigher.singletonWeigher())
                    /*任何移除缓存操作后的监听器*/
                    .removalListener((k,v,cause)->{
                        System.out.println(String.format("已删除 k ：%s v ：%s  case：%s thread %s ",k,v,cause.toString(),Thread.currentThread().getName()));
                    })
                    /*逐出缓存（自动失效）的原子操作期间的监听器*/
                    .evictionListener((k,v,cause)->{
                        System.out.println(String.format(" 删除中 k ：%s v ：%s  case：%s thread %s ",k,v,cause.toString(),Thread.currentThread().getName()));
                    })
                    /*初始化容量（参考hashmap的initialCapacity） 以减少扩容时的消耗*/
                    .initialCapacity(16)
                    /*启用 的 CacheStats 汇总计算 （具有一定的性能消耗）*/
                    .recordStats()
                    //.recordStats(()-> StatsCounter.disabledStatsCounter())
                    /*软引用 虚引用 使用注意 可能会被jvm回收*/
                    //.softValues()
                    //.weakKeys()
                    //.weakValues()
                    /*经过指定时间后刷新缓存数据  必须是LoadingCache类型缓存*/
                    .refreshAfterWrite(1,TimeUnit.SECONDS)
                    .scheduler(Scheduler.systemScheduler())
                    .build(new CacheLoader<String, String>() {
                        @Override
                        public @Nullable String load(@NonNull String key) throws Exception {
                            String v = UUID.randomUUID().toString();
                            System.out.printf("call load %s v %s \n" ,Thread.currentThread().getName(),v);
                            return v;
                        }

                        @Override
                        public @Nullable String reload(@NonNull String key, @NonNull String oldValue) throws Exception {
                            System.out.println("call reload "+ Thread.currentThread().getName());
                            return CacheLoader.super.reload(key, oldValue);
                        }

                        @Override
                        public @NonNull CompletableFuture<String> asyncLoad(@NonNull String key, @NonNull Executor executor) {
                            System.out.println("call asyncLoad "+ Thread.currentThread().getName());
                            return CacheLoader.super.asyncLoad(key, executor);
                        }

                        @Override
                        public @NonNull CompletableFuture<String> asyncReload(@NonNull String key, @NonNull String oldValue, @NonNull Executor executor) {
                            System.out.println("call asyncReload " + Thread.currentThread().getName());
                            return CacheLoader.super.asyncReload(key, oldValue, executor);
                        }
                    });
            System.out.println("call writeReadLoadingCache "+ Thread.currentThread().getName());
            String test1 = buildCache.getIfPresent("key");
            System.out.println("test1:"+test1);

            //get 会通过load方法获取值
            buildCache.get("key");
            String test2 = buildCache.getIfPresent("key");
            System.out.println("test2:"+test2);
            System.out.println(buildCache.stats().toString());
            /*主动触发刷新*/
            //buildCache.refresh("key");
            /*key会在1秒后允许自动刷新  refreshAfterWrite(1,TimeUnit.SECONDS)*/
            Thread.sleep(20*1000);

            String test3 = buildCache.getIfPresent("key");
            System.out.println("test3:"+test3);
            System.out.println(buildCache.stats().toString());

            String test4 = buildCache.getIfPresent("key");
            System.out.println("test4:"+test4);
            System.out.println(buildCache.stats().toString());

            Thread.sleep(5*1000);
        } catch (InterruptedException e) {
            //nothing to do
        }
    }

    public static void writeReadAsyncLoadingCache(){
            try {
                ForkJoinPool forkJoinPool = new ForkJoinPool(5);
                AsyncLoadingCache<String, String> buildCache = Caffeine.newBuilder()
                        /*缓存条数限制*/
                        .maximumSize(1)
                        /*缓存项在创建后一段时间内没有被写入，应自动从缓存中删除这个缓存项*/
                        //.expireAfterWrite(20, TimeUnit.SECONDS)
                        /*缓存项在创建后一段时间内若没有被写入或者读取，应自动从缓存中删除这个缓存项*/
                        .expireAfterAccess(20, TimeUnit.SECONDS)
                        /*指定缓存数据在创建/更新/读取后延长或缩短数据的过期时间 不可与 expireAfterWrite expireAfterAccess 同时使用*/
                        /*.expireAfter(new Expiry<String, String>() {
                        @Override
                        public long expireAfterCreate(@NonNull String key, @NonNull String value, long currentTime) {
                            if("key".equals(key)){
                                return 60*1000;
                            }
                            return 0;
                        }

                        @Override
                        public long expireAfterUpdate(@NonNull String key, @NonNull String value, long currentTime, @NonNegative long currentDuration) {
                            return 120*1000;
                        }

                        @Override
                        public long expireAfterRead(@NonNull String key, @NonNull String value, long currentTime, @NonNegative long currentDuration) {
                            if("key".equals(key)){
                                return 60*1000;
                            }
                            return 0;
                        }
                    })*/
                        //.expireAfterWrite()
                        /*缓存权重配置 不可与maximumSize同时使用 配合Weigher接口可以自定义缓存键值的权重值的实现*/
                        //.maximumWeight(100)
                        //.weigher(Weigher.singletonWeigher())
                        /*任何移除缓存操作后的监听器*/
                        .removalListener((k, v, cause) -> {
                            System.out.println(String.format("已删除 k ：%s v ：%s  case：%s thread %s ", k, v, cause.toString(), Thread.currentThread().getName()));
                        })
                        /*逐出缓存（自动失效）的原子操作期间的监听器*/
                        .evictionListener((k, v, cause) -> {
                            System.out.println(String.format(" 删除中 k ：%s v ：%s  case：%s thread %s ", k, v, cause.toString(), Thread.currentThread().getName()));
                        })
                        /*初始化容量（参考hashmap的initialCapacity） 以减少扩容时的消耗*/
                        .initialCapacity(16)
                        /*启用 的 CacheStats 汇总计算 （具有一定的性能消耗）*/
                        .recordStats()
                        //.recordStats(()-> StatsCounter.disabledStatsCounter())
                        /*软引用 虚引用 使用注意 可能会被jvm回收*/
                        //.softValues()
                        //.weakKeys()
                        //.weakValues()
                        .executor(forkJoinPool)
                        .scheduler(Scheduler.systemScheduler())
                        /*经过指定时间后刷新缓存数据  必须是LoadingCache类型缓存*/
                        .refreshAfterWrite(1,TimeUnit.SECONDS)
                        .buildAsync(new CacheLoader<String, String>() {
                            @Override
                            public @Nullable String load(@NonNull String key) throws Exception {
                                String v = UUID.randomUUID().toString();
                                System.out.printf("call load %s v %s \n", Thread.currentThread().getName(), v);
                                return v;
                            }

                            @Override
                            public @Nullable String reload(@NonNull String key, @NonNull String oldValue) throws Exception {
                                System.out.println("call reload " + Thread.currentThread().getName());
                                return CacheLoader.super.reload(key, oldValue);
                            }

                            @Override
                            public @NonNull CompletableFuture<String> asyncLoad(@NonNull String key, @NonNull Executor executor) {
                                System.out.println("call asyncLoad " + Thread.currentThread().getName());
                                return CacheLoader.super.asyncLoad(key, executor);
                            }

                            @Override
                            public @NonNull CompletableFuture<String> asyncReload(@NonNull String key, @NonNull String oldValue, @NonNull Executor executor) {
                                System.out.println("call asyncReload " + Thread.currentThread().getName());
                                return CacheLoader.super.asyncReload(key, oldValue, executor);
                            }
                        });
                System.out.println("call writeReadLoadingCache "+ Thread.currentThread().getName());
                CompletableFuture<String> test1 = buildCache.getIfPresent("key");
                System.out.println(String.format("test1 %s" ,test1));
                CompletableFuture<String> test2 = buildCache.get("key", (key, pool) -> {
                    CompletableFuture<String> voidCompletableFuture = CompletableFuture.supplyAsync(() -> {
                        String string = UUID.randomUUID().toString();
                        System.out.println(String.format("get create v %s thread %s",string,Thread.currentThread().getName()));
                        return string;
                    }, pool);
                    return voidCompletableFuture;
                });
                /*返回 LoadingCache*/
                LoadingCache<String, String> synchronous = buildCache.synchronous();
                buildCache.put("key",CompletableFuture.supplyAsync(() -> {
                    String string = UUID.randomUUID().toString();
                    System.out.println(String.format("put create v %s thread %s",string,Thread.currentThread().getName()));
                    return string;
                }, forkJoinPool));
                CompletableFuture<String> test3= buildCache.getIfPresent("key");

                //do somthing
                Thread.sleep(10*1000);
                CompletableFuture.allOf(test2,test3).join();
                System.out.println(String.format("test2 %s" ,test2.get()));
                System.out.println(String.format("test3 %s" ,test3.get()));
                /*test4会变 因为会在触发自动刷新*/
                CompletableFuture<String> test4= buildCache.getIfPresent("key");
                System.out.println(String.format("test4 %s" ,test4.get(10,TimeUnit.SECONDS)));
                Thread.sleep(5*1000);
                forkJoinPool.shutdown();
            } catch (InterruptedException | ExecutionException |TimeoutException e) {
                //nothing to do
            }
    }
}
