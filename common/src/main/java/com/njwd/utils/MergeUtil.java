package com.njwd.utils;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @Description
 * @Date 2019/7/25 20:11
 * @Author 朱小明
 */
public class MergeUtil {

    /**
     * 把sourceList里面的一些属性合并到targetList里面
     * 基于testFunction的条件,合入逻辑实现为biConsumer
     *
     * @param targetList
     * @param sourceList
     * @param applyFunction
     * @param biConsumer
     * @param <T>
     * @param <S>
     */
    @Deprecated
    public static <T, S> void merge(List<T> targetList, List<S> sourceList, BiFunction<? super T, ? super S, Boolean> applyFunction,
                                    BiConsumer<? super T, ? super S> biConsumer) {
        targetList.forEach((t) -> {
            Optional<S> optional = sourceList.parallelStream().filter(s -> applyFunction.apply(t, s)).findFirst();
            if (optional.isPresent()) {
                biConsumer.accept(t, optional.get());
            }
        });

    }

    /**
     * 把sourceList里面的一些item分类合并到targetList的每一个item里面
     *
     * @param targetList
     * @param sourceList
     * @param applyFunction
     * @param biConsumer
     * @param <T>
     * @param <S>
     */
    @Deprecated
    public static <T, S> void mergeList(List<T> targetList, List<S> sourceList, BiFunction<? super T, ? super S, Boolean> applyFunction,
                                        BiConsumer<? super T, ? super List<S>> biConsumer) {

        targetList.forEach((t) -> {
            List<S> dataList = sourceList.stream().filter(s -> applyFunction.apply(t, s)).collect(Collectors.toList());
            Optional<List<S>> optional = Optional.of(dataList);
            if (optional.isPresent()) {
                biConsumer.accept(t, optional.get());
            }
        });
    }

    /**
     * 将sourceList里匹配的属性放入targetList的每个元素中
     *
     * @param targetList targetList
     * @param sourceList sourceList
     * @param targetFunc targetFunc
     * @param sourceFunc sourceFunc
     * @param biConsumer biConsumer
     * @author xyyxhcj@qq.com
     * @date 2019/10/25 12:05
     **/
    public static <T, S, K> void merge(List<T> targetList, List<S> sourceList, Function<T, K> targetFunc,
                                          Function<S, K> sourceFunc, BiConsumer<? super T, ? super S> biConsumer) {
        Map<K, S> sourceMap = new LinkedHashMap<>();
        for (S s : sourceList) {
            K key = sourceFunc.apply(s);
            if (key != null) {
                sourceMap.put(key, s);
            }
        }
        for (T t : targetList) {
            K key = targetFunc.apply(t);
            if (key != null) {
                S s = sourceMap.get(key);
                if (s != null) {
                    biConsumer.accept(t, s);
                }
            }
        }
    }

    /**
     * 将sourceList以key分类整理成Map<key, List<S>>,再将List<S>放到targetList对应的item中
     *
     * @param targetList targetList
     * @param sourceList sourceList
     * @param targetFunc targetFunc
     * @param sourceFunc sourceFunc
     * @param biConsumer biConsumer
     * @author xyyxhcj@qq.com
     * @date 2019/10/25 9:30
     **/
    public static <T, S, K> void mergeList(List<T> targetList, List<S> sourceList, Function<T, K> targetFunc,
                                           Function<S, K> sourceFunc, BiConsumer<? super T, ? super List<S>> biConsumer) {
        Map<K, List<S>> sourceMap = new LinkedHashMap<>();
        for (S s : sourceList) {
            K key = sourceFunc.apply(s);
            if (key != null) {
                sourceMap.computeIfAbsent(key, k -> new LinkedList<>()).add(s);
            }
        }
        for (T t : targetList) {
            K key = targetFunc.apply(t);
            if (key != null) {
                List<S> sources = sourceMap.get(key);
                if (sources != null) {
                    biConsumer.accept(t, sources);
                }
            }
        }
    }
}
