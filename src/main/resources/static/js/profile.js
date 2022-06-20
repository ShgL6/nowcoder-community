// $(function(){
// 	$(".follow-btn").click(follow());
// });



function follow(followeeId,btn) {

	$.post(
		CONTEXT_PATH + "/user/follow",
		{"followeeId":followeeId},
		function(data){
			data = $.parseJSON(data);

			if(data.code == 0){
				// 关注或取消关注成功
				if(data.status == 1){
					// 关注TA
					$(btn).text("已关注").removeClass("btn-info").addClass("btn-secondary");
				}else{
					// 取消关注
					$(btn).text("关注TA").removeClass("btn-secondary").addClass("btn-info");
				}
				window.location.reload();
			}else{
				// 关注失败
				alert(data.msg);
			}
		}
	)

}
