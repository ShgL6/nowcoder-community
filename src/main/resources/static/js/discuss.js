
function like(btn,entityType,entityId,targetUserId) {


    $.post(
        CONTEXT_PATH + "/like",
        {"entityType":entityType,"entityId":entityId,"targetUserId":targetUserId},
        function (data) {
            data = $.parseJSON(data);
            //回显信息
            $("#hintBody").text(data.msg);
            // 显示提示框
            $("#hintModal").modal("show");

            setTimeout(function () {
                $("#hintModal").modal("hide");
                if(data.code == 0){
                    $(btn).children("b").text(data.likeStatus == 1?"已赞":"赞");
                    $(btn).children("i").text(data.likeCount);
                }
            },2000);
        }
    )
}