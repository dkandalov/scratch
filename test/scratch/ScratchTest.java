package scratch;

import org.junit.Test;

import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;

/**
 * User: dima
 * Date: 12/02/2013
 */
public class ScratchTest {
	@Test public void creatingScratches() {
		Scratch scratch = Scratch.createFrom("scratch.txt");
		assertThat(scratch.name, equalTo("scratch"));
		assertThat(scratch.extension, equalTo("txt"));

		scratch = Scratch.createFrom("&scratch.txt");
		assertThat(scratch.name, equalTo("scratch"));
		assertThat(scratch.extension, equalTo("txt"));

		scratch = Scratch.createFrom("scratch.t&xt");
		assertThat(scratch.name, equalTo("scratch"));
		assertThat(scratch.extension, equalTo("txt"));

		scratch = Scratch.createFrom("scratch");
		assertThat(scratch.name, equalTo("scratch"));
		assertThat(scratch.extension, equalTo(""));
	}
}
