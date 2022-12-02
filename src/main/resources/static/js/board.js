const delete_elements = document.getElementsByClassName("delete");
const update_elements = document.getElementsByClassName("update");
Array.from(delete_elements).forEach(function (element) {
    element.addEventListener('click', function () {
        const uri = this.dataset.uri;
        if (confirm("삭제하시겠습니까?")) {
            const articleId = $('#articleId').val();
            const articleCommentId = $('#articleCommentId').val();
            $.ajax({
                type: "DELETE",
                url: uri,
                data: JSON.stringify({"articleId": articleId, "articleCommentId": articleCommentId}),
                contentType: "application/json; charset=utf-8",
                success: function (data) {
                    if(data == "articleDeleting Success"){
                    alert('게시글이 삭제되었습니다.');
                    location.href = "/articles";
                }else if(data == "articleCommentDeleting Success"){
                    alert('댓글이 삭제되었습니다.');
                    location.reload();
                }
},
                error: function (request, status, error) {
                    alert(request.responseText);
                }
            });
        }
    });
});
function replyPut() {
    if (confirm("수정하시겠습니까?")) {
    const commentId= $('#rarticleCommentId').val();
    const articleId= $('#articleId').val();
    const content= $('#rcontent').val();
    $.ajax({
        type: "PUT",
        url: "/articles/comments/",
        data: JSON.stringify({"articleId": articleId, "articleCommentId": commentId, "commentContent": content}),
        contentType: "application/json; charset=utf-8",
        success: function (data) {
            if(data == "articleCommentUpdating Success"){
                alert('댓글이 수정되었습니다.');
                location.reload();
            }
        },
        error: function (status) {
            status.responseJSON.forEach(function () {
                $('.'+id+'ucommentContentCheck').text(this.message);
                $('.'+id+'ucommentContentCheck').css('display', 'block');
            } )
           }
    });
}
}

function replyDelete(id) {
    if (confirm("삭제하시겠습니까?")) {
        const articleId= $('#articleId').val();
        $.ajax({
            type: "DELETE",
            url: "/articles/comments/",
            data: JSON.stringify({"articleId": articleId, "articleCommentId": id}),
            contentType: "application/json; charset=utf-8",
            success: function (data) {
                if(data == "articleCommentDeleting Success"){
                    alert('댓글이 삭제되었습니다.');
                    getComment();
                }
            },
            error: function (request, status, error) {
                alert(request.responseText);
            }
        })}}



function commentUpdate (id) {
        if (confirm("수정하시겠습니까?")) {
            const articleId = $('#articleId').val();
            const ucommentContent = $('#'+id+'ucommentContent').val();
            $.ajax({
                type: "PUT",
                url: "/articles/comments/",
                data: JSON.stringify({"articleId": articleId,
                    "articleCommentId": id,
                    "content": ucommentContent,
                    }),
                contentType: "application/json; charset=utf-8",
                success: function(data) {
                    alert(data) ;
                    getComment();
                    $('.'+id+'ucommentContentCheck').text('');
                    $('.'+id+'ucommentContentCheck').css('display', 'none');
                },
                error: function (status) {
                    $(status.responseJSON).each(function(){
                        $('.'+id+'ucommentContentCheck').text(this.message);
                        $('.'+id+'ucommentContentCheck').css('display', 'block');
                    })
                }
            });
        }
    }

$("#replyContent").blur(function(){
    const content = $("#replyContent").val();
    if(content.length > 100 || content.length<2){
        $(".replyContentCheck").text("댓글을 2자 이상 100자 이하로 입력해주세요.");
        $(".replyContentCheck").css("color", "red");
    }else {
        $(".replyContentCheck").text("");
        $(".replyContentCheck").css("color", "green");
    }
});
$("#ucommentContent").blur(function(){
    const content = $("#ucommentContent").val();
    if(content.length > 100 || content.length<2){
        $(".ucommentContentCheck").text("댓글을 2자 이상 100자 이하로 입력해주세요.");
        $(".ucommentContentCheck").css("color", "red");
    }else {
        $(".ucommentContentCheck").text("");
        $(".ucommentContentCheck").css("color", "green");
    }
});
function getComment(){
    $("#commentList").empty();
    const articleId = document.getElementById("articleId").value;
    $.ajax({
        type:"get",
        url:"/articles/comments/"+articleId,
        dataType:"json",
        success:function (comment) {
            console.log(comment);
            var html = "<ul class='list-group'>";
            $(comment).each(function(){
                if(this.isParent==='Y' && this.deleted==='N'){
                    html += "<li class='list-group-item comments'>";
                    html += "<div class='comment' id='"+this.id+"comment'>";
                    html += "<a href='javascript:; class='userImg'>";
                    html += "<img src='/images/img.png' width='20' height='18' style='display:inline'>";
                    html += "</a>";
                    html += "<a href='javascript:;' class='writer' style='display:inline'>" + this.userAccountDto.nickname + "</a>";
                    html += "<div class='comment-info'>";
                    html += "<span class='comment4 date'>" + getFormatDate(new Date(this.createdAt)) + "</span>";
                    html += "<div class='comment-text' id='"+this.id+"content'> " + this.content + "</div>";
                    html += "<div class='comment_etc'>";
                    html += "<div class='comment-info'>";
                    html += "<a href='javascript:;' class='btn btn-secondary btn-icon-split comment_delete' id='"+this.id+"getChildrenBtn' >";
                    html += "<button id='"+this.id+"getChildrenBtn' onclick='getChildrenComment("+this.id+")' class='btn btn'>답글("+this.children.length+")</button>";
                    html += "</a>";
                    html += "<a href='javascript:;'>";
                    html += "<button type='button' onclick='commentDelete("+this.id+")' class='delete btn btn-outline-danger'  id='"+this.id+"deleteBtn'>삭제";
                    html += "</button>";
                    html += "</a>";
                    html += "<a href='javascript:;'>";
                    html += "<button type='button'  onclick='commentUpdateForm("+this.id+")' th:if='${"+this.userAccountDto.userId+".equals(#authentication.getName())}' class='btn btn-outline-secondary'  id='"+this.id+"updateBtn'>수정";
                    html += "</button>";
                    html += "</a>";
                    html += "</div>";
                    html += "</div>";
                    html += "</div>";
                    html += "</li>";
                    html += "<div id='"+this.id+"children' class='children'></div>";
                }

            });
            html += "</ul>";
            $("#commentList").append(html);

        }
    });
}
function getFormatDate(date){
    var year = date.getFullYear();              //yyyy
    var month = (1 + date.getMonth());          //M
    month = month >= 10 ? month : '0' + month;  //month 두자리로 저장
    var day = date.getDate();                   //d
    day = day >= 10 ? day : '0' + day;          //day 두자리로 저장
    return  year + '-' + month + '-' + day;       //'-' 추가하여 yyyy-mm-dd 형태 생성 가능
}

getComment();

function commentDelete (id) {
    if (confirm("삭제하시겠습니까?")) {
        $.ajax({
            type: "DELETE",
            url: "/articles/comments/",
            data: JSON.stringify({"articleCommentId": id}),
            contentType: "application/json; charset=utf-8",
            success: function (data) {
                alert('댓글이 삭제되었습니다.');
                getComment();
            }
            ,
            error: function (xhr, status, error) {
                alert(status.message);
            }
        });
    }
}

function commentUpdateForm(id){
    $("#"+id+"updateBtn").hide();
    $.ajax({
        type: "GET",
        url: "/articles/comments/c/"+id,
        dataType: "json",
        success: function (data) {
            var html = "<div class='comment-update'>";
            html += "<div class='comment-update-form'>";
            html += "<textarea class='form-control' id='"+id+"ucommentContent' rows='3' placeholder='댓글을 입력하세요.'>"+data.content+"</textarea>";
            html += "<div class='field-error "+id+"ucommentContentCheck'>";
            html += "</div>";
            html += "<div class='comment-update-btn'>";
            html += "<button type='button' class='btn btn-secondary'  onclick='commentUpdate("+data.id+")'>수정</button>";
            html += "<button type='button' class='btn btn-secondary' onclick='commentUpdateCancel("+data.id+")'>취소</button>";
            html += "</div>";
            html += "</div>";
            html += "</div>";
            $("#"+data.id+"content").html(html);
        },
        error: function (xhr, status, error) {
            alert(status.message);
        }
    });
}
function commentUpdateCancel(id){
    $(".comment-update").html("");
    $("#"+id+"updateBtn").show();
    getComment();
}

function getChildrenComment(id){
    $("#"+id+"children").empty();
    $("#"+id+"children").show();

    $.ajax({
        type: "GET",
        url: "/articles/comments/c/"+id,
        dataType: "json",
        success: function (data) {
            let html = "<ul class='list-group'>";
            html +="<form >";
            html += "<input type='text' class='form-control' id='"+data.id+"replyContent' placeholder='답글을 입력하세요.'>";
            html += "<div class='field-error rcommentContentCheck'>";
            html += "</div>";
            html += "<input type='hidden' id='articleId' value='"+data.articleId+"'>";
            html += "<button type='button' class='btn btn-primary' onclick='replyCheck("+data.id+")'>답글등록</button>";
            html += "<div class='field-error "+id+"replyContentCheck'>";
            html += "</div>";
            html += "</form>";
            $(data.children).each(function () {
                if(this.deleted==='N') {
                    html += "<li class='list-group-item comments'>";
                    html += "<div class='childrenComment' id='" + this.id + "comment'>";
                    html += "<a href='javascript:; class='comment-img'>";
                    html+=  "<img src='/images/reply-ico.png' alt='답글' style='display:inline' class='replyicon'>"
                    html += "<img src='' width='50' height='50'>";
                    html += "</a>";
                    html += "<div class='comment-info'>";
                    html += "<span class='comment4 date'>" + getFormatDate(new Date(this.createdAt)) + "</span>";
                    html += "<a href='javascript:;' class='writer' style='display:inline'>" + this.userAccountDto.nickname + "</a>";
                    html += "<div class='comment-text' id='" + this.id + "content'> " + this.content + "</div>";
                    html += "<div class='comment_etc'>";
                    html += "<div class='comment-info'>";
                    html += "<a href='javascript:;'>";
                    html += "<button type='button' onclick='replyDelete(" + this.id + ")' class='delete btn btn-outline-danger'  id='" + this.id + "deleteBtn'>삭제";
                    html += "</button>";
                    html += "</a>";
                    html += "<a href='javascript:;'>";
                    html += "<button type='button'  onclick='commentUpdateForm(" + this.id + ")' class='btn btn-outline-secondary'  id='" + this.id + "updateBtn'>수정";
                    html += "</button>";
                    html += "</div>";
                    html += "</div>";
                    html += "</div>";
                    html += "</li>";
                }});
            html += "</ul>";
            $("#"+id+"children").append(html);
            $("#"+id+"children").show();
            $("#"+id+"getChildrenBtn").html("접기");
            $("#"+id+"getChildrenBtn").attr("onclick","getChildrenCommentHide("+id+")");
        },
        error: function (xhr, status, error) {
            alert(status);
        }
    });


}
function getChildrenCommentHide(id){
    $("#"+id+"children").html("");
    $("#"+id+"children").hide();
    $.ajax({
        type: "GET",
        url: "/articles/comments/c/"+id,
        dataType: "json",
        success: function (data) {
            $("#"+id+"getChildrenBtn").html("답글("+data.children.length+")");
            $("#"+id+"getChildrenBtn").attr("onclick","getChildrenComment("+id+")");
        },
        error: function (xhr, status, error) {
            alert(status);
        }
    });

}


function SirenFunction(idMyDiv) {
    var objDiv = document.getElementById(idMyDiv);
    if (objDiv.style.display == "block") {
        objDiv.style.display = "none";
    } else {
        objDiv.style.display = "block";
    }
}



function logout(){
    $.ajax({
        type : "GET",
        url: '/logout',
        success: function(data) {
            alert(data);
            location.reload();
        },
        error : function( xhr, status, error ) {
            alert(status);
        }
    })
}


const editor = new toastui.Editor({
    el: document.querySelector('#editor'),
    initialEditType: 'markdown',
    previewStyle: 'vertical',
    height: '500px',
    initialValue: '내용을 입력해주세요',
    events: {
        change: function () {
            console.log(editor.getMarkdown());
        }
    }
});




function commentCheck() {
    const content = $("#commentContent").val();
    const articleId = $("#articleId").val();
    $.ajax({
        type : "POST",
        url: '/articles/comments/'+articleId,
        async: false,
        data: JSON.stringify({
            "content": content,
            "articleId": articleId,
            "parentId" : null
        }),
        contentType : "application/json; charset=utf-8",
        success: function(data) {
            alert(data) ;
            getComment();
            $('.commentContentCheck').text('');
            $('.commentContentCheck').css('display', 'none');
        },
        error: function (status) {
            $(status.responseJSON).each(function(){
                $('.commentContentCheck').text(this.message);
                $('.commentContentCheck').css('display', 'block');
            })
        }
    })
}


function replyCheck(parentId) {
    var content = $("#"+parentId+"replyContent").val();
    var articleId = $("#articleId").val();
    $.ajax({
        type : "POST",
        url: '/articles/comments/reply',
        data: JSON.stringify({
            "content": content,
            "articleId": articleId,
            "parentId" : parentId
        }),
        contentType : "application/json; charset=utf-8",
        success: function(data) {
            alert(data) ;
            getChildrenComment((parentId))
        },
        error: function (status) {
            $(status.responseJSON).each(function(){
                $('.'+parentId+'replyContentCheck').text(this.message);
                $('.'+parentId+'replyContentCheck').css('display', 'block');
            })

        }
    }) ;
}

function likeBtn(){
    const articleId = $("#articleId").val();
    $.ajax({
        type : "POST",
        url: '/articles/'+articleId,
        success: function(data) {
            $("#likeBtn").text('추천: '+data);
        },
        error: function (request, status, error) {
            alert(request.responseText);
        }
    }) ;
}

