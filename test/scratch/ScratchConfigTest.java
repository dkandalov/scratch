package scratch;

import org.junit.Test;

import static com.intellij.util.containers.ContainerUtil.list;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;
import static scratch.ScratchConfig.DEFAULT_CONFIG;
import static scratch.ScratchConfig.DOWN;
import static scratch.ScratchConfig.UP;

/**
 * User: dima
 * Date: 14/02/2013
 */
public class ScratchConfigTest {

	private final Scratch scratch1 = Scratch.createFrom("scratch1.txt");
	private final Scratch scratch2 = Scratch.createFrom("scratch2.txt");
	private final Scratch scratch3 = Scratch.createFrom("scratch3.txt");
	private final ScratchConfig config = DEFAULT_CONFIG.with(list(scratch1, scratch2, scratch3));

	@Test public void movingTopScratchToBottom() {
		assertThat(config.move(scratch1, UP), equalTo(DEFAULT_CONFIG.with(list(scratch2, scratch3, scratch1))));
	}

	@Test public void movingBottomScratchToTop() {
		assertThat(config.move(scratch3, DOWN), equalTo(DEFAULT_CONFIG.with(list(scratch3, scratch1, scratch2))));
	}

	@Test public void movingScratchUp() {
		assertThat(config.move(scratch2, UP), equalTo(DEFAULT_CONFIG.with(list(scratch2, scratch1, scratch3))));
	}
}
