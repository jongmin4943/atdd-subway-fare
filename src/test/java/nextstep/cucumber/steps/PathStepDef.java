package nextstep.cucumber.steps;

import io.cucumber.java8.En;
import nextstep.common.api.PathApiHelper;
import nextstep.cucumber.AcceptanceContext;
import nextstep.path.domain.PathType;
import nextstep.station.application.dto.StationResponse;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.assertj.core.api.SoftAssertions.assertSoftly;

public class PathStepDef implements En {
    @Autowired
    private AcceptanceContext context;

    public PathStepDef() {
        Given("{string}과 {string}의 경로를 조회하면", (final String source, final String target) -> {
            final Long sourceId = context.store.get(source, StationResponse.class).getId();
            final Long targetId = context.store.get(target, StationResponse.class).getId();
            context.response = PathApiHelper.findPath(sourceId, targetId, PathType.DISTANCE);
        });
        Then("{string} 경로와 거리 {int}이 조회된다", (final String pathString, final Integer distance) -> {
            final List<String> split = List.of(pathString.split(","));
            assertSoftly(softly -> {
                softly.assertThat(context.response.jsonPath().getList("stations.name", String.class)).containsExactly(split.toArray(new String[0]));
                softly.assertThat(context.response.jsonPath().getInt("distance")).isEqualTo(distance);
            });
        });
    }
}
