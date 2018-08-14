package io.github.isuru.oasis.model.configs;

/**
 * @author iweerarathna
 */
public class ConfigKeys {

    public static final String KEY_RABBIT_HOST = "rabbit.host";
    public static final String KEY_RABBIT_PORT = "rabbit.port";
    public static final String KEY_RABBIT_VIRTUAL_HOST = "rabbit.virtualhost";
    public static final String KEY_RABBIT_USERNAME = "rabbit.username";
    public static final String KEY_RABBIT_PASSWORD = "rabbit.password";

    public static final String KEY_RABBIT_SRC_EXCHANGE_NAME = "rabbit.src.exchange";
    public static final String KEY_RABBIT_SRC_EXCHANGE_TYPE = "rabbit.src.exchange.type";
    public static final String KEY_RABBIT_SRC_EXCHANGE_DURABLE = "rabbit.src.exchange.durable";

    public static final String KEY_RABBIT_QUEUE_OUTPUT_DURABLE = "rabbit.queue.output.durable";
    public static final String KEY_RABBIT_QUEUE_SRC = "rabbit.queue.src";

    public static final String KEY_RABBIT_QUEUE_OUT_POINTS = "rabbit.queue.points";
    public static final String KEY_RABBIT_QUEUE_OUT_BADGES = "rabbit.queue.badges";
    public static final String KEY_RABBIT_QUEUE_OUT_MILESTONES = "rabbit.queue.milestones";
    public static final String KEY_RABBIT_QUEUE_OUT_MILESTONESTATES = "rabbit.queue.milestonestates";
    public static final String KEY_RABBIT_QUEUE_OUT_CHALLENGES = "rabbit.queue.challenges";


    public static final int DEF_RABBIT_PORT = 5672;
    public static final String DEF_RABBIT_VIRTUAL_HOST = "oasis";
    public static final String DEF_RABBIT_SRC_EXCHANGE_TYPE = "fanout";
    public static final boolean DEF_RABBIT_SRC_EXCHANGE_DURABLE = true;

    public static final String DEF_RABBIT_Q_POINTS_SINK = "game.o{gid}.points";
    public static final String DEF_RABBIT_Q_BADGES_SINK = "game.o{gid}.badges";
    public static final String DEF_RABBIT_Q_MILESTONES_SINK = "game.o{gid}.milestones";
    public static final String DEF_RABBIT_Q_MILESTONESTATE_SINK = "game.o{gid}.milestonestates";
    public static final String DEF_RABBIT_Q_CHALLENGES_SINK = "game.o{gid}.challenges";
}
