package com.gmoon.springschedulingquartz.quartz.job_store.constants;

import com.gmoon.springschedulingquartz.exception.NotSupportedInitializerException;

public final class QuartzColumnLength {
  public static final int SCHEDULER_NAME = 120;
  public static final int LOCK_NAME = 40;
  public static final int JOB_NAME = 200;
  public static final int JOB_GROUP_NAME = 200;
  public static final int DESCRIPTION = 250;
  public static final int JOB_CLASS_NAME = 250;
  public static final int TRIGGER_NAME = 200;
  public static final int TRIGGER_GROUP = 200;
  public static final int JOB_GROUP = 200;
  public static final int TRIGGER_STATE = 16;
  public static final int TRIGGER_TYPE = 8;
  public static final int CALENDAR_NAME = 200;
  public static final int MISFIRE_INSTR = 2;
  public static final int REPEAT_COUNT = 7;
  public static final int REPEAT_INTERVAL = 12;
  public static final int TIMES_TRIGGERED = 10;
  public static final int CRON_EXPRESSION = 200;
  public static final int TIME_ZONE_ID = 80;
  public static final int STR_PROP_1 = 512;
  public static final int STR_PROP_2 = 512;
  public static final int STR_PROP_3 = 512;
  public static final int ENTRY_ID = 95;
  public static final int INSTANCE_NAME = 200;
  public static final int FIRED_TIME = 13;
  public static final int SCHED_TIME = 13;
  public static final int SATE = 16;
  public static final int LAST_CHECKIN_TIME = 13;
  public static final int CHECKIN_INTERVAL = 13;

  private static final int OPTION = 1;
  public static final int IS_DURABLE = OPTION;
  public static final int IS_NONCONCURRENT = OPTION;
  public static final int IS_UPDATE_DATA = OPTION;
  public static final int REQUESTS_RECOVERY = OPTION;
  public static final int BOOL_PROP_1 = OPTION;
  public static final int BOOL_PROP_2 = OPTION;

  private static final int BIGINT = 13;
  public static final int END_TIME = BIGINT;
  public static final int NEXT_FIRE_TIME = BIGINT;
  public static final int PREV_FIRE_TIME = BIGINT;
  public static final int START_TIME = BIGINT;

  private QuartzColumnLength() {
    throw new NotSupportedInitializerException();
  }
}
