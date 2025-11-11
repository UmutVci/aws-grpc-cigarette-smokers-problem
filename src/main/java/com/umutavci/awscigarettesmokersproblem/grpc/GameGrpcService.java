package com.umutavci.awscigarettesmokersproblem.grpc;

import com.umutavci.awscigarettesmokersproblem.service.PlayService;
import lombok.RequiredArgsConstructor;
import net.devh.boot.grpc.server.service.GrpcService;

@GrpcService
@RequiredArgsConstructor
public class GameGrpcService extends GameServiceGrpc.GameServiceImplBase {

    private final PlayService playService;

    @Override
    public void joinGame(JoinRequest request, StreamObserver<JoinResponse> responseObserver) {
        String result = playService.userWantToPlay(request.getUsername());
        JoinResponse response = JoinResponse.newBuilder().setMessage(result).build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void joinSpecificTable(JoinSpecificRequest request, StreamObserver<JoinResponse> responseObserver) {
        String result = playService.joinSpecificTable(request.getUsername(), request.getTableId());
        JoinResponse response = JoinResponse.newBuilder().setMessage(result).build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void leaveTable(LeaveRequest request, StreamObserver<LeaveResponse> responseObserver) {
        String result = playService.leaveTable(request.getUsername(), request.getTableId());
        LeaveResponse response = LeaveResponse.newBuilder().setMessage(result).build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void getTableStatus(TableRequest request, StreamObserver<TableResponse> responseObserver) {
        Table table = playService.getTableRepository()
                .findById(request.getTableId())
                .orElse(null);

        TableResponse.Builder builder = TableResponse.newBuilder().setTableId(request.getTableId());
        if (table != null) {
            builder.setStarted(table.getIsStarted())
                    .setBooked(table.getIsBooked())
                    .setPlayerCount(table.getSmokers().size());
        } else {
            builder.setStarted(false).setBooked(false).setPlayerCount(0);
        }

        responseObserver.onNext(builder.build());
        responseObserver.onCompleted();
    }
}
