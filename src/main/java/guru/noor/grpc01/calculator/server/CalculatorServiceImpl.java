package guru.noor.grpc01.calculator.server;

import com.proto.calculator.CalculatorServiceGrpc;

import com.proto.calculator.SumRequest;
import com.proto.calculator.SumResponse;
import io.grpc.stub.StreamObserver;

public class CalculatorServiceImpl extends CalculatorServiceGrpc.CalculatorServiceImplBase {

    @Override
    public void sum(SumRequest request, StreamObserver<SumResponse> responseObserver) {
        SumResponse response = SumResponse.newBuilder()
                .setResult(request.getFirstNum() + request.getSecondNum())
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }
}
