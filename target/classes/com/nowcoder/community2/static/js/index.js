$(function(){
	$("#publishBtn").click(publish);
});

function publish() {
	$("#publishModal").modal("hide");
	$("#hintModal").modal("show");

	//获取标签和内容
	var title = $("#recipient-name").val();
	var content = $("#message-text").val();

	//发送异步请求
	$.post(
		CONTEXT_PATH + "/discussPost/post",
		{"title":title,"content":content},
		function (data) {
			//转换对象
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