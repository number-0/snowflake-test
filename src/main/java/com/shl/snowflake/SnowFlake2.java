package com.shl.snowflake;

/**
 * twitter的snowflake算法 -- java实现
 * 改动：5位机器标识和5位数据中心 合起来变成10位
 * @author beyond
 * @date 2016/11/26
 */
public class SnowFlake2 {

    /**
     * 起始的时间戳
     */
    private final static long START_STMP = 1631013419666L;

    /**
     * 每一部分占用的位数
     */
    private final static long SEQUENCE_BIT = 12; //序列号占用的位数
//    private final static long MACHINE_BIT = 5;   //机器标识占用的位数
//    private final static long DATACENTER_BIT = 5;//数据中心占用的位数
    private final static long MACHINE_IP_BIT = 10;   //机器ip标识占用的位数

    /**
     * 每一部分的最大值
     */
//    private final static long MAX_DATACENTER_NUM = -1L ^ (-1L << DATACENTER_BIT);
//    private final static long MAX_MACHINE_NUM = -1L ^ (-1L << MACHINE_BIT);
    private final static long MAX_MACHINE_IP_NUM = -1L ^ (-1L << MACHINE_IP_BIT);
    private final static long MAX_SEQUENCE = -1L ^ (-1L << SEQUENCE_BIT);


    /**
     * 每一部分向左的位移
     */
//    private final static long MACHINE_LEFT = SEQUENCE_BIT;
//    private final static long DATACENTER_LEFT = SEQUENCE_BIT + MACHINE_BIT;
    private final static long MACHINE_IP_LEFT = SEQUENCE_BIT;
    private final static long TIMESTMP_LEFT = MACHINE_IP_LEFT + MACHINE_IP_BIT;

//    private long datacenterId;  //数据中心
    private long machineId;     //机器标识
    private long sequence = 0L; //序列号
    private long lastStmp = -1L;//上一次时间戳

    public SnowFlake2(long machineId) {
        if (machineId > MAX_MACHINE_IP_NUM || machineId < 0) {
            throw new IllegalArgumentException("machineId can't be greater than " + MAX_MACHINE_IP_NUM + " or less than 0");
        }
        this.machineId = machineId;
    }

    /**
     * 产生下一个ID
     *
     * @return
     */
    public synchronized long nextId() {
        long currStmp = getNewstmp();
        //出现时钟回拨，抛异常
        if (currStmp < lastStmp) {
            throw new RuntimeException("clock moved backwards, refusing to generate id.");
        }

        if (currStmp == lastStmp) {
            //相同毫秒内，序列号自增  在时间戳一样的情况下，递增序列号
            //&的目的是判断是否超过，如果超过的情况下，则会从0重新开始
            sequence = (sequence + 1) & MAX_SEQUENCE;
            //同一毫秒的序列数已经达到最大
            //同一毫秒序列号已用完，则等待并获取下一毫秒
            if (sequence == 0L) {
                currStmp = getNextMill();
            }
        } else {
            //不同毫秒内，序列号置为0
            sequence = 0L;
        }

        lastStmp = currStmp;

        return (currStmp - START_STMP) << TIMESTMP_LEFT //时间戳部分
//                | datacenterId << DATACENTER_LEFT       //数据中心部分
                | machineId << MACHINE_IP_LEFT             //机器标识部分
                | sequence;                             //序列号部分
    }

    private long getNextMill() {
        long mill = getNewstmp();
        while (mill <= lastStmp) {
            mill = getNewstmp();
        }
        return mill;
    }

    private long getNewstmp() {
        return System.currentTimeMillis();
    }

    public static void main(String[] args) {
        SnowFlake2 snowFlake = new SnowFlake2(1023);

        for (int i = 0; i < (1 << 12); i++) {
            System.out.println(snowFlake.nextId());
        }

    }
}
