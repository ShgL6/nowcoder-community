$(function(){
	$("#sendBtn").click(send_letter);
	$(".close").click(delete_msg);
});

function send_letter() {
	$("#sendModal").modal("hide");
	$("#hintModal").modal("show");

	// 获取收信人和内容
    var toUsername = $("#recipient-name").val();
    var content = $("#message-text").val();


	//发送异步请求
	$.post(
		CONTEXT_PATH + "/message/letter/send",
		{"toUsername":toUsername,"content":content},
		function (data) {
			data = $.parseJSON(data);
			//回显信息
			$("#hintBody").text(data.msg);
			// 显示提示框
			$("#hintModal").modal("show");

			setTimeout(function(){
				$("#hintModal").modal("hide");
				//刷新页面
				if(data.code == 0){
					window.location.reload();
				}
			}, 2000);

		}
	)



}

function delete_msg() {
	// TODO 删除数据
	$(this).parents(".media").remove();
}