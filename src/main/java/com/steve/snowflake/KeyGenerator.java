package com.steve.snowflake;

import com.google.common.base.Preconditions;
import jdk.nashorn.internal.objects.annotations.Setter;
import lombok.extern.slf4j.Slf4j;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * @Description:
 * @Author: stevejobson
 * @CreateDate: 2018/1/6 下午3:40
 */
public class KeyGenerator {

        public static final long EPOCH;

        //序列位，每毫秒生成的key的数量，2^12 = 4096
        private static final long SEQUENCE_BITS = 12L;
        //工作进程位
        private static final long WORKER_ID_BITS = 10L;

        private static final long SEQUENCE_MASK = (1 << SEQUENCE_BITS) - 1;

        private static final long WORKER_ID_LEFT_SHIFT_BITS = SEQUENCE_BITS;

        private static final long TIMESTAMP_LEFT_SHIFT_BITS = WORKER_ID_LEFT_SHIFT_BITS + WORKER_ID_BITS;

        private static final long WORKER_ID_MAX_VALUE = 1L << WORKER_ID_BITS;

        private static long workerId;

        static {
            Calendar calendar = Calendar.getInstance();
            calendar.set(2016, Calendar.NOVEMBER, 1);
            calendar.set(Calendar.HOUR_OF_DAY, 0);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 0);
            EPOCH = calendar.getTimeInMillis();
        }

        private long sequence;

        private long lastTime;

        /**
         * Set work process id.
         *
         * @param workerId work process id
         */
        public static void setWorkerId(final long workerId) {
            Preconditions.checkArgument(workerId >= 0L && workerId < WORKER_ID_MAX_VALUE);
            KeyGenerator.workerId = workerId;
        }

        /**
         * Generate key.
         *
         * @return key type is @{@link Long}.
         */
        public synchronized Number generateKey() {
            long currentMillis = System.currentTimeMillis();
            Preconditions.checkState(lastTime <= currentMillis, "Clock is moving backwards, last time is %d milliseconds, current time is %d milliseconds", lastTime, currentMillis);
            if (lastTime == currentMillis) {
                if (0L == (sequence = ++sequence & SEQUENCE_MASK)) {
                    currentMillis = waitUntilNextTime(currentMillis);
                }
            } else {
                sequence = 0;
            }
            lastTime = currentMillis;

            return ((currentMillis - EPOCH) << TIMESTAMP_LEFT_SHIFT_BITS) | (workerId << WORKER_ID_LEFT_SHIFT_BITS) | sequence;
        }

        private long waitUntilNextTime(final long lastTime) {
            long time = System.currentTimeMillis();
            while (time <= lastTime) {
                time = System.currentTimeMillis();
            }
            return time;
        }
    }