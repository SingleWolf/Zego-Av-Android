package com.icbc.roomservice.kernelcode;

/**
 * 结果码对象，包含结果码和结果码对应的信息
 */
public class ResultCode {

    private int mCode;
    private String mMsg;

    private ResultCode(int code, String msg) {
        this.mCode = code;
        this.mMsg = msg;
    }

    /**
     * @return 返回结果 是否成功
     */
    public boolean isSuccess() {
        return this.mCode == 0;
    }

    /**
     * @return 返回错误码
     */
    public int getCode() {
        return this.mCode;
    }


    /**
     * @return 返回错误码对应等信息
     */
    public String getMsg() {
        return this.mMsg;
    }

    /**
     * 根据 登录队列服务结果码 生成结果对象
     *
     * @param loginQueueResult 登录队列服务结果码
     * @return 结果码对象
     */
    static ResultCode createResultCodeByLoginQueueResult(int loginQueueResult) {
        String msg;
        switch (loginQueueResult) {
            case 0:
                msg = "success";
                break;
            case 1:
                msg = "socket ConnectError";
                break;
            case 2:
                msg = "socket reconnect error";
                break;
            case 3:
                msg = "login request error";
                break;
            case 4:
                msg = "login timeout error";
                break;
            case 5:
                msg = "heart beat timeout error";
                break;
            case 6:
                msg = "network broken error";
                break;
            case 7:
                msg = "dispatch error";
                break;
            case 10000002:
                msg = "input params error";
                break;
            case 10000003:
                msg = "input params length limit";
                break;
            case 10000004:
                msg = "gocache error";
                break;
            case 10000101:
                msg = "data format error";
                break;
            case 10000102:
                msg = "login token error";
                break;
            case 10000103:
                msg = "login token expired";
                break;
            case 10001001:
                msg = "no user session_id";
                break;
            case 10001002:
                msg = "user state error";
                break;
            case 10001003:
                msg = "user session_id wrong";
                break;
            case 10001004:
                msg = "user session_key expire";
                break;
            case 10001005:
                msg = "user get error";
                break;
            case 10001006:
                msg = "user create session_id error";
                break;
            case 10001007:
                msg = "not find user session_id";
                break;
            case 10001008:
                msg = "user signature error";
                break;
            default:
                msg = "undefined error";
                break;
        }
        return new ResultCode(loginQueueResult, msg);
    }

    /**
     * 根据 队列结果码 生成 结果码对象
     *
     * @param queueResultCode 队列结果码
     * @return 结果码对象
     */
    static ResultCode createResultCodeByQueueResult(int queueResultCode) {
        String msg;
        switch (queueResultCode) {
            case 0:
                msg = "success";
                break;
            case -99:
                msg = "sdk protocol parse error";
                break;
            case -100:
                msg = "sdk protocol serialize error";
                break;
            case -111:
                msg = "send msg error";
                break;
            case 1:
                msg = "failure";
                break;
            case 2:
                msg = "input params error";
                break;
            case 3:
                msg = "input params length limit";
                break;
            case 4:
                msg = "gocache error";
                break;
            case 5001:
                msg = "queue get error";
                break;
            case 5002:
                msg = "queue set error";
                break;
            case 5003:
                msg = "queue del error";
                break;
            case 5004:
                msg = "queue num limit";
                break;
            case 5010:
                msg = "queue user role error";
                break;
            case 5011:
                msg = "queue list get error";
                break;
            case 5012:
                msg = "queue list set error";
                break;
            case 5013:
                msg = "queue list del error";
                break;
            case 5101:
                msg = "queue staff get error";
                break;
            case 5102:
                msg = "queue staff set error";
                break;
            case 5103:
                msg = "queue staff not exist";
                break;
            case 5104:
                msg = "queue staff num limit";
                break;
            case 5201:
                msg = "queue customer get error";
                break;
            case 5202:
                msg = "queue customer set error";
                break;
            case 5203:
                msg = "queue customer not exist";
                break;
            case 5204:
                msg = "queue no customer";
                break;
            case 5205:
                msg = "queue add customer error";
                break;
            case 5206:
                msg = "queue customer del error";
                break;
            case 5207:
                msg = "queue customer has offline";
                break;
            case 5208:
                msg = "queue full";
                break;
            case 5209:
                msg = "queue customer passed";
                break;
            case 5301:
                msg = "queue push customer arrive errer";
                break;
            case 5302:
                msg = "queue push staff update error";
                break;
            case 5303:
                msg = "queue push update error";
                break;
            case 5401:
                msg = "queue consult get session error";
                break;
            case 5402:
                msg = "queue consult set session error";
                break;
            case 5403:
                msg = "queue consult del session error";
                break;
            case 5404:
                msg = "queue consult check session error";
                break;
            case 6004:
                msg = "limit: queue count was exceeded";
                break;
            case 6005:
                msg = "limit: queue staff count was exceeded";
                break;
            case 6006:
                msg = "limit: queue customer count was exceeded";
                break;
            default:
                msg = "undefined error";
                break;
        }
        return new ResultCode(queueResultCode, msg);
    }

    /**
     * 根据 踢出房间原因码 生成 结果码对象
     *
     * @param kickOutReason 队列结果码
     * @return 结果码对象
     */
    static ResultCode createResultCodeByKickOutReason(int kickOutReason) {
        String msg;
        switch (kickOutReason) {
            case 16777219:
                msg = "账户多点登录被踢出";
                break;
            case 16777220:
                msg = "被主动踢出";
                break;
            case 16777221:
                msg = "房间会话错误被踢出";
                break;
            default:
                msg = "undefined error";
                break;
        }
        return new ResultCode(kickOutReason, msg);
    }

    /**
     * 根据 房间相关错误回调错误码 生成结果码对象
     *
     * @param roomResult 房间相关错误回调错误码
     * @return 结果码对象
     */
    static ResultCode createResultCodeByRoomResult(int roomResult) {
        String msg;
        switch (roomResult) {
            case 10002001:
                msg = "no room head";
                break;
            case 10002002:
                msg = "no room id";
                break;
            case 10002003:
                msg = "no rooms id";
                break;
            case 10002004:
                msg = "no room user session id";
                break;
            case 10002005:
                msg = "room not found";
                break;
            case 10002006:
                msg = "room user not found";
                break;
            case 10002007:
                msg = "room stream not found";
                break;
            case 10002008:
                msg = "room_sid wrong";
                break;
            case 10002009:
                msg = "room user_sid wrong";
                break;
            case 10002010:
                msg = "room stream_sid wrong";
                break;
            case 10002011:
                msg = "room push custom msg error";
                break;
            case 10006001:
                msg = "limit: no right to create room";
                break;
            case 10006002:
                msg = "limit: room count was exceeded";
                break;
            case 10006003:
                msg = "limit: room user count was exceeded";
                break;
            case 10006007:
                msg = "limit: stream publish count was exceeded";
                break;
            case 10006008:
                msg = "limit: stream play count was exceeded";
                break;
            default:
                msg = "undefined error";
                break;
        }

        return new ResultCode(roomResult, msg);
    }

    /**
     * 根据 房间onDisconnect Reason 生成结果码对象
     *
     * @param disconnectReason 房间相关错误回调错误码
     * @return 结果码对象
     */
    static ResultCode createResultCodeByRoomDisconnectReason(int disconnectReason) {
        String msg;
        switch (disconnectReason) {
            case 16777219:
                msg = "disconnect with server";
                break;
            default:
                msg = "undefined error";
                break;
        }

        return new ResultCode(disconnectReason, msg);
    }

    /**
     * 根据 推流状态码 生成结果码对象
     *
     * @param publishState 推流状态码
     * @return 结果码对象
     */
    static ResultCode createResultCodeByPublishState(int publishState) {
        String msg;
        switch (publishState) {
            case 3:
                msg = "直播遇到严重问题（如出现，请联系 ZEGO 技术支持）。";
                break;
            case 4:
                msg = "创建直播流失败。";
                break;
            case 5:
                msg = "获取流信息失败。";
                break;
            case 6:
                msg = "无流信息。";
                break;
            case 7:
                msg = "媒体服务器连接失败（请确认推流端是否正常推流、正式环境和测试环境是否设置同一个、网络是否正常）。";
                break;
            case 8:
                msg = "DNS 解析失败。";
                break;
            case 9:
                msg = "未登录就直接拉流。";
                break;
            case 10:
                msg = "逻辑服务器网络错误(网络断开时间过长时容易出现此错误)。";
                break;
            case 105:
                msg = "发布流名被占用。";
                break;
            default:
                msg = "undefined error";
                break;
        }
        return new ResultCode(publishState, msg);
    }

    /**
     * 根据 播放流状态码 生成结果码对象
     *
     * @param PlayState 播放流状态码
     * @return 结果码对象
     */
    static ResultCode createResultCodeByPlayState(int PlayState) {
        String msg;
        switch (PlayState) {
            case 3:
                msg = "直播遇到严重问题（如出现，请联系 ZEGO 技术支持）。";
                break;
            case 4:
                msg = "创建直播流失败。";
                break;
            case 5:
                msg = "获取流信息失败。";
                break;
            case 6:
                msg = "无流信息。";
                break;
            case 7:
                msg = "媒体服务器连接失败（请确认推流端是否正常推流、正式环境和测试环境是否设置同一个、网络是否正常）。";
                break;
            case 8:
                msg = "DNS 解析失败。";
                break;
            case 9:
                msg = "未登录就直接拉流。";
                break;
            case 10:
                msg = "逻辑服务器网络错误(网络断开时间过长时容易出现此错误)。";
                break;
            default:
                msg = "undefined error";
                break;
        }
        return new ResultCode(PlayState, msg);
    }

    static ResultCode createResultCodeByDeviceError(int deviceError) {
        String msg;
        switch (deviceError) {
            case 1:
                msg = "麦克风设备没有授权";
                break;
            case 2:
                msg = "摄像头设备没有授权";
                break;
            case 3:
                msg = "正在通话";
                break;
            default:
                msg = "undefined error";
                break;
        }
        return new ResultCode(deviceError, msg);
    }

    @Override
    public String toString() {
        return "ResultCode{" +
                "mCode=" + mCode +
                ", mMsg='" + mMsg + '\'' +
                '}';
    }
}
