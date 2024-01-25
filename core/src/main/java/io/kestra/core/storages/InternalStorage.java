package io.kestra.core.storages;

import org.slf4j.Logger;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Files;
import java.util.List;
import java.util.Optional;

/**
 * The default {@link Storage} implementation acting as a facade to the {@link StorageInterface}.
 */
public class InternalStorage implements Storage {

    private static final String PATH_SEPARATOR = "/";
    private static final String CACHE_ARCHIVE_NAME = "cache.zip";

    private final Logger logger;
    private final StorageContext context;
    private final StorageInterface storage;

    /**
     * Creates a new {@link InternalStorage} instance.
     *
     * @param logger  The logger to be used by this class.
     * @param context The storage context.
     * @param storage The storage to delegate operations.
     */
    public InternalStorage(Logger logger, StorageContext context, StorageInterface storage) {
        this.logger = logger;
        this.context = context;
        this.storage = storage;
    }

    /** {@inheritDoc} **/
    @Override
    public boolean isFileExist(URI uri) {
        return this.storage.exists(context.getTenantId(), uri);
    }

    /** {@inheritDoc} **/
    @Override
    public InputStream getFile(final URI uri) throws IOException {
        if (uri == null) {
            throw new IllegalArgumentException("Invalid internal storage uri, got null");
        }

        String scheme = uri.getScheme();
        if (scheme == null) {
            throw new IllegalArgumentException("Invalid internal storage uri, got uri '" + uri + "'");
        }

        if (!scheme.equals("kestra")) {
            throw new IllegalArgumentException("Invalid internal storage scheme, got uri '" + uri + "'");
        }

        return this.storage.get(context.getTenantId(), uri);

    }

    /** {@inheritDoc} **/
    @Override
    public List<URI> deleteExecutionFiles() throws IOException {
        return this.storage.deleteByPrefix(context.getTenantId(), context.getExecutionStorageURI());
    }

    /** {@inheritDoc} **/
    @Override
    public URI getContextBaseURI() {
        return this.context.getContextStorageURI();
    }

    /** {@inheritDoc} **/
    @Override
    public URI putFile(InputStream inputStream, String name) throws IOException {
        URI uri = context.getContextStorageURI();
        URI resolved =  uri.resolve(uri.getPath() + PATH_SEPARATOR + name);
        return this.storage.put(context.getTenantId(), resolved, new BufferedInputStream(inputStream));
    }

    /** {@inheritDoc} **/
    @Override
    public URI putFile(InputStream inputStream, URI uri) throws IOException {
        return this.storage.put(context.getTenantId(), uri, new BufferedInputStream(inputStream));
    }

    /** {@inheritDoc} **/
    @Override
    public URI putFile(File file) throws IOException {
        return putFile(file, null);
    }

    /** {@inheritDoc} **/
    @Override
    public URI putFile(File file, String name) throws IOException {
        URI uri = context.getContextStorageURI();
        URI resolved =  uri.resolve(uri.getPath() + PATH_SEPARATOR + (name != null ? name : file.getName()));
        try (InputStream is = new FileInputStream(file)) {
            return putFile(is, resolved);
        } finally {
            try {
                Files.delete(file.toPath());
            } catch (IOException e) {
                logger.warn("Failed to delete temporary file '{}'", file.toPath(), e);
            }
        }
    }

    private String taskStateFilePathPrefix(String name, Boolean isNamespace, Boolean useTaskRun) {
        // Ugly
        String taskRunValue = (this.context instanceof StorageContext.Task taskContext) ? taskContext.getTaskRunValue() : null;
        return PATH_SEPARATOR + this.storage.statePrefix(
            this.context.getNamespace(),
            isNamespace ? null : this.context.getFlowId(),
            name,
            useTaskRun ? taskRunValue : null
        );
    }

    /** {@inheritDoc} **/
    @Override
    public InputStream getTaskStateFile(String state, String name) throws IOException {
        return this.getTaskStateFile(state, name, false, true);
    }

    /** {@inheritDoc} **/
    @Override
    public InputStream getTaskStateFile(String state, String name, Boolean isNamespace, Boolean useTaskRun) throws IOException {
        URI uri = URI.create(this.taskStateFilePathPrefix(state, isNamespace, useTaskRun));
        URI resolve = uri.resolve(uri.getPath() + PATH_SEPARATOR + name);

        return this.storage.get(context.getTenantId(), resolve);
    }

    /** {@inheritDoc} **/
    @Override
    public URI putTaskStateFile(byte[] content, String state, String name) throws IOException {
        return this.putTaskStateFile(content, state, name, false, true);
    }

    /** {@inheritDoc} **/
    @Override
    public URI putTaskStateFile(byte[] content, String state, String name, Boolean namespace, Boolean useTaskRun) throws IOException {
        try (InputStream inputStream = new ByteArrayInputStream(content)) {
            return this.putFile(
                inputStream,
                this.taskStateFilePathPrefix(state, namespace, useTaskRun),
                name
            );
        }
    }

    /** {@inheritDoc} **/
    @Override
    public URI putTaskStateFile(File file, String state, String name) throws IOException {
        return this.putTaskStateFile(file, state, name, false, true);
    }

    /** {@inheritDoc} **/
    @Override
    public URI putTaskStateFile(File file, String state, String name, Boolean isNamespace, Boolean useTaskRun) throws IOException {
        return this.putFile(
            file,
            this.taskStateFilePathPrefix(state, isNamespace, useTaskRun),
            name
        );
    }

    /** {@inheritDoc} **/
    @Override
    public boolean deleteTaskStateFile(String state, String name) throws IOException {
        return this.deleteTaskStateFile(state, name, false, true);
    }

    /** {@inheritDoc} **/
    @Override
    public boolean deleteTaskStateFile(String state, String name, Boolean isNamespace, Boolean useTaskRun) throws IOException {
        URI uri = URI.create(this.taskStateFilePathPrefix(state, isNamespace, useTaskRun));
        URI resolve = uri.resolve(uri.getPath() + PATH_SEPARATOR + name);

        return this.storage.delete(context.getTenantId(), resolve);
    }

    /** {@inheritDoc} **/
    @Override
    public Optional<InputStream> getTaskCacheFile(String namespace, String flowId, String taskId, String value) throws IOException {
        URI uri = URI.create(PATH_SEPARATOR + this.storage.cachePrefix(namespace, flowId, taskId, value) + PATH_SEPARATOR + CACHE_ARCHIVE_NAME);
        return isFileExist(uri) ?
            Optional.of(this.storage.get(context.getTenantId(), uri)) :
            Optional.empty();
    }

    /** {@inheritDoc} **/
    @Override
    public Optional<Long> getTaskCacheFileLastModifiedTime(String namespace, String flowId, String taskId, String value) throws IOException {
        URI uri = URI.create(PATH_SEPARATOR + this.storage.cachePrefix(namespace, flowId, taskId, value) + PATH_SEPARATOR + CACHE_ARCHIVE_NAME);
        return isFileExist(uri) ?
            Optional.of(this.storage.getAttributes(context.getTenantId(), uri).getLastModifiedTime()) :
            Optional.empty();
    }

    /** {@inheritDoc} **/
    @Override
    public URI putTaskCacheFile(File file, String namespace, String flowId, String taskId, String value) throws IOException {
        String prefix = PATH_SEPARATOR + this.storage.cachePrefix(namespace, flowId, taskId, value);
        return this.putFile(file, prefix, CACHE_ARCHIVE_NAME);
    }

    /** {@inheritDoc} **/
    @Override
    public Optional<Boolean> deleteTaskCacheFile(String namespace, String flowId, String taskId, String value) throws IOException {
        URI uri = URI.create(PATH_SEPARATOR + this.storage.cachePrefix(namespace, flowId, taskId, value) + PATH_SEPARATOR + CACHE_ARCHIVE_NAME);
        return isFileExist(uri) ?
            Optional.of(this.storage.delete(context.getTenantId(), uri)) :
            Optional.empty();
    }

    private URI putFile(File file, String prefix, String name) throws IOException {
        try (InputStream fileInput = new FileInputStream(file)) {
            return this.putFile(fileInput, prefix, (name != null ? name : file.getName()));
        } finally {
            try {
                Files.delete(file.toPath());
            } catch (IOException e) {
                logger.warn("Failed to delete temporary file '{}'", file.toPath(), e);
            }
        }
    }

    private URI putFile(InputStream inputStream, String prefix, String name) throws IOException {
        URI uri = URI.create(prefix);
        URI resolve = uri.resolve(uri.getPath() + PATH_SEPARATOR + name);

        return this.storage.put(context.getTenantId(), resolve, new BufferedInputStream(inputStream));
    }
}
