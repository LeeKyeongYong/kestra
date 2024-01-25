package io.kestra.core.storages;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.List;
import java.util.Optional;

/**
 * Service interface for using Kestra's storage.
 */
public interface Storage {

    /**
     * Checks whether the given URI points to an exiting file/object in the internal storage.
     *
     * @param uri the URI of the file/object in the internal storage.
     * @return {@code true} if the URI points to a file/object that exists in the internal storage.
     */
    boolean isFileExist(URI uri);

    /**
     * Retrieve an {@link InputStream} for the given file URI.
     *
     * @param uri the file URI.
     * @return the {@link InputStream}.
     * @throws IllegalArgumentException if the given {@link URI} is {@code null} or invalid.
     * @throws IOException              if an error happens while accessing the file.
     */
    InputStream getFile(URI uri) throws IOException;

    /**
     * Deletes all the files for the current execution.
     *
     * @return The URIs of the deleted files.
     * @throws IOException if an error happened while deleting files.
     */
    List<URI> deleteExecutionFiles() throws IOException;

    /**
     * Gets the storage base URI for the current context.
     *
     * @return the URI.
     */
    URI getContextBaseURI();

    /**
     * Stores a file with the given name for the given {@link InputStream} into Kestra's storage.
     *
     * @param inputStream the {@link InputStream} of the file content.
     * @param name        the target name of the file to be stored in the storage.
     * @return the URI of the file/object in the internal storage.
     * @throws IOException if an error happened while storing the file.
     */
    URI putFile(InputStream inputStream, String name) throws IOException;

    /**
     * Stores a file with the given name for the given {@link InputStream} into Kestra's storage.
     *
     * @param inputStream the {@link InputStream} of the file content.
     * @param uri         the target URI of the file to be stored in the storage.
     * @return the URI of the file/object in the internal storage.
     * @throws IOException if an error occurred while storing the file.
     */
    URI putFile(InputStream inputStream, URI uri) throws IOException;

    /**
     * Put the temporary file on storage and delete it after.
     *
     * @param file the temporary file to upload to storage
     * @return the {@code StorageObject} created
     * @throws IOException If the temporary file can't be read
     */
    URI putFile(File file) throws IOException;

    /**
     * Put the temporary file on storage and delete it after.
     *
     * @param file the temporary file to upload to storage
     * @param name overwrite file name
     * @return the {@code StorageObject} created
     * @throws IOException If the temporary file can't be read
     */
    URI putFile(File file, String name) throws IOException;

    // =================================================================================================================
    //  STATE STORE
    // =================================================================================================================
    InputStream getTaskStateFile(String state, String name) throws IOException;

    InputStream getTaskStateFile(String state, String name, Boolean isNamespace, Boolean useTaskRun) throws IOException;

    URI putTaskStateFile(byte[] content, String state, String name) throws IOException;

    URI putTaskStateFile(byte[] content, String state, String name, Boolean namespace, Boolean useTaskRun) throws IOException;

    URI putTaskStateFile(File file, String state, String name) throws IOException;

    URI putTaskStateFile(File file, String state, String name, Boolean isNamespace, Boolean useTaskRun) throws IOException;

    boolean deleteTaskStateFile(String state, String name) throws IOException;

    boolean deleteTaskStateFile(String state, String name, Boolean isNamespace, Boolean useTaskRun) throws IOException;


    // =================================================================================================================
    //  TASK CACHE
    // =================================================================================================================

    /**
     * Get from the internal storage the cache file corresponding to this task.
     * If the cache file didn't exist, an empty Optional is returned.
     *
     * @param namespace the flow namespace
     * @param flowId    the flow identifier
     * @param taskId    the task identifier
     * @param value     optional, the task run value
     * @return an Optional with the cache input stream or empty.
     */
    Optional<InputStream> getTaskCacheFile(String namespace, String flowId, String taskId, String value) throws IOException;

    Optional<Long> getTaskCacheFileLastModifiedTime(String namespace, String flowId, String taskId, String value) throws IOException;

    /**
     * Put into the internal storage the cache file corresponding to this task.
     *
     * @param file      the cache as a ZIP archive
     * @param namespace the flow namespace
     * @param flowId    the flow identifier
     * @param taskId    the task identifier
     * @param value     optional, the task run value
     * @return the URI of the file inside the internal storage.
     */
    URI putTaskCacheFile(File file, String namespace, String flowId, String taskId, String value) throws IOException;

    Optional<Boolean> deleteTaskCacheFile(String namespace, String flowId, String taskId, String value) throws IOException;
}
