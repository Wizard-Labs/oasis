package io.github.isuru.oasis.services.utils.local.sinks;

import io.github.isuru.oasis.injector.ConsumerUtils;
import io.github.isuru.oasis.injector.model.ChallengeModel;
import io.github.isuru.oasis.model.db.IOasisDao;

import java.util.Map;

/**
 * @author iweerarathna
 */
public class ChallengeSink extends BaseLocalSink {

    ChallengeSink(IOasisDao dao, long gameId) {
        super(dao, gameId, LocalSinks.SQ_CHALLENGES);
    }

    @Override
    protected void handle(String value) throws Exception {
        ChallengeModel model = mapper.readValue(value, ChallengeModel.class);
        Map<String, Object> data = ConsumerUtils.toChallengeDaoData(getGameId(), model);
        dao.executeCommand("game/addChallengeWinner", data);
    }
}