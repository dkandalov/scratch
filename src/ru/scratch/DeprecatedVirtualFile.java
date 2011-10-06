package ru.scratch;

import org.jetbrains.annotations.NotNull;

import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;

/**
 * // TODO hack
 * This is decompiled com.intellij.openapi.vfs.DeprecatedVirtualFile class to make plugin work in IntelliJ 11.
 *
 * @author DKandalov
 */
public abstract class DeprecatedVirtualFile extends VirtualFile {
	@NotNull
	@Override
	public String getUrl() {
		return VirtualFileManager.constructUrl(getFileSystem().getProtocol(), getPath());
	}

	@Override
	public boolean isInLocalFileSystem() {
		return getFileSystem() instanceof LocalFileSystem;
	}
}
