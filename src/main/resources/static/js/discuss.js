
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


function beTop(btn,postId) {

    $.post(
        CONTEXT_PATH + "/discussPost/top",
        {"postId" : postId},
        function (response) {
            response = $.parseJSON(response);
            //回显信息
            $("#hintBody").text(response.msg);
            // 显示提示框
            $("#hintModal").modal("show");

            setTimeout(function () {
                $("#hintModal").modal("hide");
                if(response.code == 0){
                    $(btn).attr("disabled",true);
                }
            },2000);
        }
    )
}

function wonder(btn,postId) {

    $.post(
        CONTEXT_PATH + "/discussPost/wonder",
        {"postId" : postId},
        function (response) {
            response = $.parseJSON(response);
            //回显信息
            $("#hintBody").text(response.msg);
            // 显示提示框
            $("#hintModal").modal("show");

            setTimeout(function () {
                $("#hintModal").modal("hide");
                if(response.code == 0){
                    $(btn).attr("disabled",true);
                }
            },2000);
        }
    )
}

function del(btn,postId) {

    $.post(
        CONTEXT_PATH + "/discussPost/delete",
        {"postId" : postId},
        function (response) {
            response = $.parseJSON(response);
            //回显信息
            $("#hintBody").text(response.msg);
            // 显示提示框
            $("#hintModal").modal("show");

            setTimeout(function () {
                $("#hintModal").modal("hide");
                if(response.code == 0){
                    $(btn).attr("disabled",true);
                }
            },2000);
        }
    )
}