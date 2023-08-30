package lambda;

import basic.Request;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.google.cloud.functions.HttpFunction;
import com.google.cloud.functions.HttpRequest;
import com.google.cloud.functions.HttpResponse;
import org.jclouds.ContextBuilder;
import org.jclouds.blobstore.BlobStore;
import org.jclouds.blobstore.BlobStoreContext;
import saaf.Inspector;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;

import static basic.MetadataRetriever.*;
import static basic.ObjectStorage.deleteContainer;

public class DeleteContainers implements RequestHandler<Request, HashMap<String, Object>>, HttpFunction {
    static BlobStoreContext blobContext;
    static BlobStore blobStore;
    private static Long initializeConnectionTime;

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
        for (int i = 0; i < count; i++) {
            try {
                deleteContainer(blobStore, containerName + i);
            } catch (Exception e) {
                e.printStackTrace();
                actual_count = i;
                connect = false;
                inspector.addAttribute("duration", new Date().getTime() - initializeConnectionTime);
                break;
            }
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
