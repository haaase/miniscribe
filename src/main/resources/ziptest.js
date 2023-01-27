import {
    BlobReader,
    ZipReader,
    ZipWriter,
} from "@zip.js/zip.js";

export async function doStuff() {
    console.log("Yooo")
    // ----
    // Write the zip file
    // ----

    // Creates a TransformStream object, the zip content will be written in the
    // `writable` property.
    const zipFileStream = new TransformStream();
    // Creates a Promise object resolved to the zip content returned as a Blob
    // object retrieved from `zipFileStream.readable`.
    const zipFileBlobPromise = new Response(zipFileStream.readable).blob();
    // Creates a ReadableStream object storing the text of the entry to add in the
    // zip (i.e. "Hello world!").
    const helloWorldReadable = new Blob(["Hello world!"]).stream();

    // Creates a ZipWriter object writing data into `zipFileStream.writable`, adds
    // the entry "hello.txt" containing the text "Hello world!" retrieved from
    // `helloWorldReadable`, and closes the writer.
    const zipWriter = new ZipWriter(zipFileStream.writable);
    await zipWriter.add("hello.txt", helloWorldReadable);
    await zipWriter.close();

    // Retrieves the Blob object containing the zip content into `zipFileBlob`.
    const zipFileBlob = await zipFileBlobPromise;

    // ----
    // Read the zip file
    // ----

    // Creates a BlobReader object used to read `zipFileBlob`.
    const zipFileReader = new BlobReader(zipFileBlob);
    // Creates a TransformStream object, the content of the first entry in the zip
    // will be written in the `writable` property.
    const helloWorldStream = new TransformStream();
    // Creates a Promise object resolved to the content of the first entry returned
    // as text from `helloWorldStream.readable`.
    const helloWorldTextPromise = new Response(helloWorldStream.readable).text();

    // Creates a ZipReader object reading the zip content via `zipFileReader`,
    // retrieves metadata (name, dates, etc.) of the first entry, retrieves its
    // content into `helloWorldStream.writable`, and closes the reader.
    const zipReader = new ZipReader(zipFileReader);
    const firstEntry = (await zipReader.getEntries()).shift();
    await firstEntry.getData(helloWorldStream.writable);
    await zipReader.close();

    // Displays "Hello world!".
    const helloWorldText = await helloWorldTextPromise;
    console.log(helloWorldText);
}