/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ru.scratch;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.jetbrains.annotations.NotNull;

import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.fileTypes.FileTypes;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileSystem;
import com.intellij.testFramework.LightVirtualFile;

/**
 * @author Dmitry Kandalov
 */
class ScratchVirtualFile extends DeprecatedVirtualFile {
	private static final String SCRATCH = "scratch";
	private final LightVirtualFile lightVirtualFile;
	private final int index;

	public ScratchVirtualFile(String text, int index) {
		this.index = index;
		lightVirtualFile = new LightVirtualFile(getName(), FileTypes.PLAIN_TEXT, text);
	}

	@NotNull
	public OutputStream getOutputStream(Object requestor, long newModificationStamp, long newTimeStamp) throws IOException {
		final OutputStream outputStream = lightVirtualFile.getOutputStream(requestor, newModificationStamp, newTimeStamp);
		return new FilterOutputStream(outputStream) {
			@Override
			public void close() throws IOException {
				super.close();
				ScratchData.getInstance().setScratchText(index, outputStream.toString());
			}
		};
	}

	@NotNull
	@Override
	public String getName() {
		if (index == 0) {
			return SCRATCH + ".txt";
		} else {
			return SCRATCH + "_" + (index + 1) + ".txt";
		}
	}

	@NotNull
	public VirtualFileSystem getFileSystem() {
		return lightVirtualFile.getFileSystem();
	}

	@NotNull
	public FileType getFileType() {
		return lightVirtualFile.getFileType();
	}

	public String getPath() {
		return lightVirtualFile.getPath();
	}

	@Override
	public boolean isWritable() {
		return lightVirtualFile.isWritable();
	}

	@Override
	public boolean isDirectory() {
		return lightVirtualFile.isDirectory();
	}

	@Override
	public boolean isValid() {
		return lightVirtualFile.isValid();
	}

	@Override
	public VirtualFile getParent() {
		return lightVirtualFile.getParent();
	}

	@Override
	public VirtualFile[] getChildren() {
		return lightVirtualFile.getChildren();
	}

	@Override
	public InputStream getInputStream() throws IOException {
		return lightVirtualFile.getInputStream();
	}

	@NotNull
	@Override
	public byte[] contentsToByteArray() throws IOException {
		return lightVirtualFile.contentsToByteArray();
	}

	@Override
	public long getModificationStamp() {
		return lightVirtualFile.getModificationStamp();
	}

	@Override
	public long getTimeStamp() {
		return lightVirtualFile.getTimeStamp();
	}

	@Override
	public long getLength() {
		return lightVirtualFile.getLength();
	}

	@Override
	public void refresh(boolean asynchronous, boolean recursive, Runnable postRunnable) {
		lightVirtualFile.refresh(asynchronous, recursive, postRunnable);
	}

	public void rename(Object requestor, @NotNull String newName) throws IOException {
		lightVirtualFile.rename(requestor, newName);
	}
}
