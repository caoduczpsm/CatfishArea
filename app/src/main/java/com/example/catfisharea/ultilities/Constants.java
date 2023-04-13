package com.example.catfisharea.ultilities;

import java.util.HashMap;

public class Constants {

    public static final String KEY_PREFERENCE_NAME = "catFishAreaPreference";
    public static final String REMOTE_MSG_TYPE = "type";

    public static final String KEY_NOW = "now";

    // Các thông tin người dùng
    public static final String KEY_COLLECTION_USER = "users";
    public static final String KEY_IS_SIGNED_IN = "isSignedIn";
    public static final String KEY_NAME = "name";
    public static final String KEY_PHONE = "phone";
    public static final String KEY_PASSWORD = "password";
    public static final String KEY_USER_ID = "userId";
    public static final String KEY_IMAGE = "image";
    public static final String KEY_FCM_TOKEN = "fcmToken";
    public static final String KEY_ADDRESS = "address";
    public static final String KEY_DATEOFBIRTH = "dateOfBirth";
    public static final String KEY_PERSONAL_ID = "personalID";

    // Các thông tin công ty
    public static final String KEY_COLLECTION_COMPANY = "companies";
    public static final String KEY_COMPANY_ID = "companyID";
    public static final String KEY_COMPANY_NAME = "companyName";
    public static final String KEY_COMPANY_ADDRESS = "companyAddress";
    public static final String KEY_COMPANY_IMAGE = "companyImage";
    public static final String KEY_COMPANY_CODE = "companyCode";
    public static final String KEY_COMPANY_PHONE = "companyPhone";
    public static final String KEY_COMPANY_TOTAL_ACCOUNT = "totalAccount";
    public static final String KEY_COMPANY_AMOUNT_ADMIN = "amountAdmin";
    public static final String KEY_COMPANY_AMOUNT_ACCOUNTANT = "amountAccountant";
    public static final String KEY_COMPANY_AMOUNT_REGIONAL_CHIEF = "amountRegionalChief";
    public static final String KEY_COMPANY_AMOUNT_DIRECTOR = "amountDirector";
    public static final String KEY_COMPANY_AMOUNT_WORKER = "amountWorker";

    // Các chức vụ trong vùng nuôi (dịch từ GG)
    public static final String KEY_TYPE_ACCOUNT = "typeAccount"; // Loại tài khoản
    public static final String KEY_ADMIN = "admin"; // Admin
    public static final String KEY_REGIONAL_CHIEF = "regionalChief"; // Trưởng Vùng
    public static final String KEY_DIRECTOR = "director"; // Trưởng Khu
    public static final String KEY_ACCOUNTANT = "accountant"; // Kế Toán
    public static final String KEY_WORKER = "worker"; // Công Nhân
    public static final String KEY_USER = "user"; // Người dùng cá nhân

    // Lưu trạng thái đăng ký cá nhân hay công ty
    public static final String KEY_TYPE_REGIS = "typeRegis"; // Người dùng cá nhân
    public static final String KEY_PERSONAL_REGIS = "personalRegis"; // Trạng thái đăng ký cho cá nhân
    public static final String KEY_COMPANY_REGIS = "companyRegis"; // Trạng thái đăng ký cho công ty

    // Kiểm tra dữ liệu người dùng trên firebase có trống không
    public static final String KEY_CHECK_AVAILABLE_DATA = "checkAvailableData"; // Trạng thái tài khoản mới tạo cần cập nhật dữ liệu
    public static final String KEY_CHECK_AVAILABLE_TYPE_ACCOUNT = "checkAvailableTypeAccount"; // Trạng thái loại tài khoản vừa tạo

    //    Quản lý vùng
    public static final String KEY_COLLECTION_AREA = "areas";
    public static final String KEY_COLLECTION_CAMPUS = "campus";
    public static final String KEY_COLLECTION_POND = "ponds";
    public static final String KEY_AREA_ID = "areaID";
    public static final String KEY_NUM_OF_FEEDING = "numOfFeeding";
    public static final String KEY_NUM_OF_FEEDING_LIST = "numOfFeedingList";
    public static final String KEY_AMOUNT_FED = "amountFed";
    public static final String KEY_SPECIFICATIONS_TO_MEASURE = "specificationsToMeasure";
    public static final String KEY_SPECIFICATIONS_MEASURED = "measuredParameters";
    public static final String KEY_SPECIFICATION_PH = "pH";
    public static final String KEY_SPECIFICATION_SALINITY = "salinity";
    public static final String KEY_SPECIFICATION_ALKALINITY = "alkalinity";
    public static final String KEY_SPECIFICATION_TEMPERATE = "temperate";
    public static final String KEY_SPECIFICATION_H2S = "h2S";
    public static final String KEY_SPECIFICATION_NH3 = "nH3";

    public static final String KEY_MAP = "geo";
    public static final String KEY_AREA = "area";
    public static final String KEY_CAMPUS = "campus";
    public static final String KEY_POND = "pond";
    public static final String KEY_CAMPUS_ID = "campusID";
    public static final String KEY_POND_ID = "pondID";


    // Kiểu chọn ngày
    public static final String KEY_DAY_START_TASK = "dayStartTask";
    public static final String KEY_DAY_END_TASK = "dayEndTask";
    public static final String KEY_DAY_SELECTED = "daySelected";
    public static final String KEY_MONTH_SELECTED = "monthSelected";
    public static final String KEY_YEAR_SELECTED = "yearSelected";

    // Các trưởng dữ liệu nhiệm vụ
    public static final String KEY_COLLECTION_TASK = "tasks";
    public static final String KEY_COLLECTION_FIXED_TASK = "fixedTasks";
    public static final String KEY_CREATOR_ID = "creatorID";
    public static final String KEY_CREATOR_NAME = "creatorName";
    public static final String KEY_CREATOR_IMAGE = "creatorImage";
    public static final String KEY_CREATOR_PHONE = "creatorPhone";
    public static final String KEY_RECEIVER_ID = "receiverID";
    public static final String KEY_RECEIVER_NAME = "receiverName";
    public static final String KEY_RECEIVER_IMAGE = "receiverImage";
    public static final String KEY_RECEIVER_PHONE = "receiverPhone";
    public static final String KEY_TASK_CONTENT = "content";
    public static final String KEY_STATUS_TASK = "status";
    public static final String KEY_COMPLETED = "completed";
    public static final String KEY_UNCOMPLETED = "uncompleted";
    public static final String KEY_AMOUNT_RECEIVERS_COMPLETED = "amountReceiversCompleted";
    public static final String KEY_AMOUNT_RECEIVERS = "amountReceivers";
    public static final String KEY_RECEIVERS_ID_COMPLETED = "receiversCompleted";
    public static final String KEY_TASK_TITLE = "title";
    public static final String KEY_MY_DIRECTOR_TASK = "myTasks";
    public static final String KEY_DIRECTOR_ALLOCATION_TASK = "allocationTasks";
    public static final String KEY_FIXED_TASK_FEED_FISH = "Cho cá ăn";
    public static final String KEY_FIXED_TASK_MEASURE_WATER = "Đo chất lượng nước";
    public static final String KEY_TYPE_OF_TASK = "typeOfTask";

    // Các trường dữ liệu bình luận nhiệm vụ
    public static final String KEY_COLLECTION_COMMENT_TASK = "commentTasks";
    public static final String KEY_COMMENT_ID = "commentID";
    public static final String KEY_COMMENT_TIMESTAMP = "timestamp";
    public static final String KEY_COMMENT_CONTENT = "content";
    public static final String KEY_USER_COMMENT_ID = "userCommentID";
    public static final String KEY_USER_COMMENT_NAME = "userCommentName";
    public static final String KEY_USER_COMMENT_IMAGE = "userCommentImage";
    public static final String KEY_USER_COMMENT_POSITION = "userCommentPosition";
    public static final String KEY_TASK_COMMENT_ID = "taskID";

    public static final String KEY_COLLECTION_CHAT = "chats";
    public static final String KEY_SENDER_ID = "senderId";
    public static final String KEY_MESSAGE = "message";
    public static final String KEY_TIMESTAMP = "timestamp";
    public static final String KEY_COLLECTION_CONVERSATIONS = "conversations";
    public static final String KEY_SENDER_NAME = "senderName";
    public static final String KEY_SENDER_IMAGE = "senderImage";
    public static final String KEY_LAST_MESSAGE = "lastMessage";
    public static final String KEY_AVAILABILITY = "availability";
    public static final String REMOTE_MSG_AUTHORIZATION = "Authorization";
    public static final String REMOTE_MSG_CONTENT_TYPE = "Content-Type";

    public static final String REMOTE_MSG_DATA = "data";
    public static final String REMOTE_MSG_REGISTRATION_IDS = "registration_ids";

    public static final String KEY_MESSAGE_TYPE = "type";
    public static final String MESSAGE_TEXT = "text";
    public static final String MESSAGE_IMAGE = "image";
    public static final String MESSAGE_FILE = "file";
    public static final String KEY_FILE = "download_file";

    public static final String REMOTE_MSG_INVITATION = "invitation";
    public static final String REMOTE_MSG_MEETING_TYPE = "meetingtype";
    public static final String REMOTE_MSG_INVITER_TOKEN = "invitertoken";

    public static final String REMOTE_MSG_INVITATION_RESPONSE = "invitationResponse";
    public static final String REMOTE_MSG_INVITATION_ACCEPTED = "accepted";
    public static final String REMOTE_MSG_INVITATION_REJECTED = "rejected";
    public static final String REMOTE_MSG_INVITATION_CANCELLED = "cancelled";

    public static final String REMOTE_MSG_MEETING_ROOM = "meetingRoom";

    public static final String KEY_COLLECTION_GROUP = "groups";
    public static final String KEY_GROUP_NAME = "groupName";
    public static final String KEY_GROUP_DESCRIPTION = "groupDescription";
    public static final String KEY_GROUP_IMAGE = "GroupImage";
    public static final String KEY_GROUP_ID = "groupId";
    public static final String KEY_GROUP_MEMBER = "groupMember";
    public static final String KEY_GROUP = "group";
    public static final String KEY_COLLECTION_CHAT_GROUPS = "chatGroups";
    public static final String KEY_COLLECTION_CONVERSATIONS_GROUP = "conversationGroups";
    public static final String KEY_GROUP_OWNER = "owner";


    public static final String KEY_PRIVATE_ACCOUNT_NAME = "privateName";
    public static final String KEY_COLLECTION_ROOM = "rooms";
    public static final String KEY_AMOUNT_OF_ROOM = "amount";
    public static final String KEY_ROOM_MEMBER = "roomMember";
    public static final String KEY_COLLECTION_PRIVATE_CHAT = "privateChats";
    public static final String KEY_SEEN_MESSAGE = "seen";

    public static final String KEY_LANGUAGE = "language";

    //    KEY REQUEST
    public static final String KEY_COLLECTION_REQUEST = "requests";
    public static final String KEY_ID_REQUEST = "idRequest";
    public static final String KEY_DATESTART_REQUESET = "dateStartRequest";
    public static final String KEY_DATEEND_REQUESET = "dateEndRequest";
    public static final String KEY_DATE_CREATED_REQUEST = "dateCreatedRequest";
    public static final String KEY_NOTE_REQUESET = "noteRequest";
    public static final String KEY_REASON_REQUESET = "reasonRequest";
    public static final String KEY_TYPE_REQUEST = "typeRequest";
    public static final String KEY_LEAVE_REQUEST = "leaveRequest";
    public static final String KEY_IMPORT_REQUEST = "importRequest";
    public static final String KEY_REQUESTER = "requeseter";
    public static final String KEY_ACCEPT = "accept";
    public static final String KEY_REFUSE = "refuse";
    public static final String KEY_PENDING = "pending";
    public static final String KEY_MATERIALSLIST = "materialsList";

    //    KEY kế hoạch nuôi trồng
    public static final String KEY_COLLECTION_PLAN = "plans";
    public static final String KEY_DATE_OF_PLAN = "date";
    public static final String KEY_ID_PLAN = "idPlan";
    public static final String KEY_ACREAGE = "acreage";
    public static final String KEY_CONSISTENCE = "consistence";
    public static final String KEY_NUMBER_OF_FISH = "numberOfFish";
    public static final String KEY_SURVIVAL_RATE = "survivalRate";
    public static final String KEY_NUMBER_OF_FISH_ALIVE = "numberOfFishAlive";
    public static final String KEY_HARVEST_SIZE = "harvestSize";
    public static final String KEY_HARVEST_YIELD = "harvestYield";
    public static final String KEY_FCR = "fcr";
    public static final String KEY_FOOD = "food";
    public static final String KEY_FINGERLING_SAMPLES = "fingerlingSamples";
    //    Kho
    public static final String KEY_COLLECTION_WAREHOUSE = "warehouses";
    public static final String KEY_WAREHOUSE_ID = "warehouseID";
    public static final String KEY_DESCRIPTION = "description";
    public static final String KEY_CATEGORY_OF_WAREHOUSE = "categories";
    //    Danh mục kho
    public static final String KEY_COLLECTION_CATEGORY = "categories";
    public static final String KEY_PRODUCER = "producer";
    public static final String KEY_UNIT = "unit";
    public static final String KEY_EFFECT = "effect";
    public static final String KEY_CATEGORY_FOOD = "Thức ăn";


    public static HashMap<String, String> remoteMsgHeaders = null;


    public static HashMap<String, String> getRemoteMsgHeaders() {
        if (remoteMsgHeaders == null) {
            remoteMsgHeaders = new HashMap<>();
            remoteMsgHeaders.put(
                    REMOTE_MSG_AUTHORIZATION,
                    "key=AAAAtfm5WI8:APA91bFgQUl08EUT3XFI_ieSpjwqoUwMkIuKDF3zNq7OQobgoJt2dYWid9ATtbJAG1AnkYVtJl8J2TkZIghnTpd4mgZewErcydXHiNGrfByPC9guSFgQ3aa6bCE-QWFJWSO9OHZiDzfW"
            );
            remoteMsgHeaders.put(
                    REMOTE_MSG_CONTENT_TYPE,
                    "application/json"
            );
        }
        return remoteMsgHeaders;
    }
}

