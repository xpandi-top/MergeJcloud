package lambda;

import basic.Request;
import com.amazonaws.services.lambda.runtime.*;
import com.google.cloud.functions.HttpFunction;
import com.google.cloud.functions.HttpRequest;
import com.google.cloud.functions.HttpResponse;
import org.jclouds.ContextBuilder;
import org.jclouds.blobstore.BlobStore;
import org.jclouds.blobstore.BlobStoreContext;
import saaf.Inspector;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Date;
import java.util.HashMap;

import static basic.FileTransform.fileGenerate;
import static basic.MetadataRetriever.*;
import static basic.ObjectStorage.writeBlob;

public class WriteObject implements RequestHandler<Request, HashMap<String, Object>>, HttpFunction {
    static BlobStoreContext blobContext;
    static BlobStore blobStore;
    static Long initializeConnectionTime;
    public HashMap<String, Object> procedure(Request request, HttpRequest httpRequest, HttpResponse httpResponse) throws IOException {
        //*******************collect initial data
        Inspector inspector = new Inspector();
        if (!isMac) {
            inspector.inspectAll();
        }
        //*************FunctionStart**************
        // get parameters
        if (provider.contains("google")) {
            request = gson.fromJson(httpRequest.getReader(), Request.class);
        }
        boolean connect = true;
        int count = 2;
        int actual_count = 0;
        if (request != null && request.getObjectName() != null) objectName = request.getObjectName();
        if (request != null && request.getContainerName() != null) containerName = request.getContainerName();
        if (request != null && request.getCount() > 0) count = request.getCount();
        // Initialize Jclouds
        if (blobContext == null) {
            blobContext = ContextBuilder.newBuilder(provider)
                    .credentials(identity, credential)
                    .buildApi(BlobStoreContext.class);
            blobStore = blobContext.getBlobStore();
            initializeConnectionTime = new Date().getTime();
        }
        //****************START FUNCTION IMPLEMENTATION*************************
        try {
            StringWriter sw = new StringWriter();
            fileGenerate(sw, count);
            writeBlob(blobStore, containerName, objectName, sw.toString());
        } catch (Exception e) {
            e.printStackTrace();
            connect = false;
            inspector.addAttribute("duration", new Date().getTime() - initializeConnectionTime);
        }
        //***************Get Result**************8
        getResponse(httpResponse, isMac, inspector, count, actual_count, connect, initializeConnectionTime);
        return inspector.finish();
    }

    // AWS Handler
    @Override
    public HashMap<String, Object> handleRequest(Request request, Context context) {
        try {
            return procedure(request, null, null);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
    // Google Handler
    @Override
    public void service(HttpRequest httpRequest, HttpResponse httpResponse) throws Exception {
        procedure(null, httpRequest, httpResponse);
    }
}
