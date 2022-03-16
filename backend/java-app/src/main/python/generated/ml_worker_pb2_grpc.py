# Generated by the gRPC Python protocol compiler plugin. DO NOT EDIT!
"""Client and server classes corresponding to protobuf-defined services."""
import grpc

import ml_worker_pb2 as ml__worker__pb2


class MLWorkerStub(object):
    """Missing associated documentation comment in .proto file."""

    def __init__(self, channel):
        """Constructor.

        Args:
            channel: A grpc.Channel.
        """
        self.loadModel = channel.unary_unary(
                '/worker.MLWorker/loadModel',
                request_serializer=ml__worker__pb2.LoadModelRequest.SerializeToString,
                response_deserializer=ml__worker__pb2.LoadModelResponse.FromString,
                )
        self.predict = channel.unary_unary(
                '/worker.MLWorker/predict',
                request_serializer=ml__worker__pb2.PredictRequest.SerializeToString,
                response_deserializer=ml__worker__pb2.PredictResponse.FromString,
                )


class MLWorkerServicer(object):
    """Missing associated documentation comment in .proto file."""

    def loadModel(self, request, context):
        """Missing associated documentation comment in .proto file."""
        context.set_code(grpc.StatusCode.UNIMPLEMENTED)
        context.set_details('Method not implemented!')
        raise NotImplementedError('Method not implemented!')

    def predict(self, request, context):
        """Missing associated documentation comment in .proto file."""
        context.set_code(grpc.StatusCode.UNIMPLEMENTED)
        context.set_details('Method not implemented!')
        raise NotImplementedError('Method not implemented!')


def add_MLWorkerServicer_to_server(servicer, server):
    rpc_method_handlers = {
            'loadModel': grpc.unary_unary_rpc_method_handler(
                    servicer.loadModel,
                    request_deserializer=ml__worker__pb2.LoadModelRequest.FromString,
                    response_serializer=ml__worker__pb2.LoadModelResponse.SerializeToString,
            ),
            'predict': grpc.unary_unary_rpc_method_handler(
                    servicer.predict,
                    request_deserializer=ml__worker__pb2.PredictRequest.FromString,
                    response_serializer=ml__worker__pb2.PredictResponse.SerializeToString,
            ),
    }
    generic_handler = grpc.method_handlers_generic_handler(
            'worker.MLWorker', rpc_method_handlers)
    server.add_generic_rpc_handlers((generic_handler,))


 # This class is part of an EXPERIMENTAL API.
class MLWorker(object):
    """Missing associated documentation comment in .proto file."""

    @staticmethod
    def loadModel(request,
            target,
            options=(),
            channel_credentials=None,
            call_credentials=None,
            insecure=False,
            compression=None,
            wait_for_ready=None,
            timeout=None,
            metadata=None):
        return grpc.experimental.unary_unary(request, target, '/worker.MLWorker/loadModel',
            ml__worker__pb2.LoadModelRequest.SerializeToString,
            ml__worker__pb2.LoadModelResponse.FromString,
            options, channel_credentials,
            insecure, call_credentials, compression, wait_for_ready, timeout, metadata)

    @staticmethod
    def predict(request,
            target,
            options=(),
            channel_credentials=None,
            call_credentials=None,
            insecure=False,
            compression=None,
            wait_for_ready=None,
            timeout=None,
            metadata=None):
        return grpc.experimental.unary_unary(request, target, '/worker.MLWorker/predict',
            ml__worker__pb2.PredictRequest.SerializeToString,
            ml__worker__pb2.PredictResponse.FromString,
            options, channel_credentials,
            insecure, call_credentials, compression, wait_for_ready, timeout, metadata)
