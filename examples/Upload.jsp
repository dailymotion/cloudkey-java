<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" >
<head>
<title>Demo SDK Java Upload</title>
<style>
#upload_container #link_upload_html {
  display: block;
  padding-top: 5px;
}
#upload_container #swfu_files {
  padding-top: 20px;
}
#upload_container #swfu_files .swfu_file {
  border-bottom: 1px dotted #cccccc;
  padding: 10px;
}
#upload_container #swfu_files .swfu_file .file_infos {
  font-size: 10px;
  line-height: 10px;
  padding-bottom: 10px;
}
#upload_container #swfu_files .swfu_file .file_infos .tool {
  margin-right: 5px;
}
#upload_container #swfu_files .swfu_file .file_infos .file_status {
  color: grey;
}
#upload_container #swfu_files .swfu_file .file_infos .file_status.error {
  color: red;
}
#upload_container #swfu_files .swfu_file .file_infos .file_status.done {
  color: #2fab09;
}
#upload_container #swfu_files .swfu_file .file_progressbar_container {
  -moz-border-radius: 6px;
  -webkit-border-radius: 6px;
  -o-border-radius: 6px;
  -ms-border-radius: 6px;
  -khtml-border-radius: 6px;
  border-radius: 6px;
  background-color: white;
  border: 1px solid #cccccc;
  height: 10px;
  padding: 1px;
}
#upload_container #swfu_files .swfu_file .file_progressbar_container .file_progressbar {
  -moz-border-radius: 5px;
  -webkit-border-radius: 5px;
  -o-border-radius: 5px;
  -ms-border-radius: 5px;
  -khtml-border-radius: 5px;
  border-radius: 5px;
  background-color: #8398ab;
  height: 10px;
  width: 0px;
}
#upload_container #swfu_files .swfu_file .file_progressbar_container .file_progressbar.error {
  background-color: #ffafae;
}
</style>
<%@ page import="com.dmcloud.*"%>
<%@ page import="java.net.InetAddress"%>
<%@ include file="Config.jsp"%>
<%
String redirect_url = "http://" + InetAddress.getLocalHost().getHostName() + ":8080/examples/MediaCreate.jsp";

CloudKey_Media media = new CloudKey_Media(user_id, api_key);
DCObject result = media.upload(true, true, redirect_url);
String status_url = result.pull("status");
String upload_url = result.pull("url");
%>
<script type="text/javascript" src="http://code.jquery.com/jquery-1.6.1.min.js"></script>
<script type="text/javascript">
        var upload_html_interval;
        $(document).ready(function()
        {
            $('#upload_html_form').submit(function() {
                // Build progress bar
                $('#swfu_files').append(
                    '<div class="swfu_file" id="file_progress">'+
                        '<div class="file_title">' + $('#upload_file').val() + '</div>'+
                        '<div class="file_infos">'+
                            '<div class="file_size float_left"></div>'+
                            '<div class="float_right"><span class="file_status">initializing</span></div>'+
                            '<div class="clear"></div>'+
                        '</div>'+
                        '<div class="file_progressbar_container">'+
                            '<div class="file_progressbar"></div>'+
                        '</div>'+
                    '</div>');

                // Ajax Poll for upload progress data
                upload_html_interval = setInterval(function() {
                    $.getJSON('<% out.write(status_url); %>', function(upload) {
                        var percent = 0;
                        var received = 0;
                        var size = 0;

                        if (typeof(upload) == 'object')
                        {
                            if (typeof(upload.state) == 'string' && upload.state != 'notfound')
                            {
                                $('#file_progress .file_status').html(upload.state);

                                if (upload.state == 'uploading')
                                {
                                    if (typeof(upload.received) != 'undefined' && typeof(upload.size) != 'undefined')
                                    {
                                        percent  = (upload.received / upload.size) * 100;
                                        percent  = Math.round(percent * 10) / 10;
                                        received = upload.received / (1048576); // 1024 * 1024 = 1048576
                                        received = Math.round(received * 10) / 10;
                                        size     = upload.size / (1048576);
                                        size     = Math.round(size * 10) / 10;

                                        $('#file_progress .file_size').html(received+'MB of '+size+'MB');
                                        $('#file_progress .file_status').html(percent+'%');
                                        $('#file_progress .file_progressbar').css({
                                            width: percent+'%'
                                        });

                                        if (received == size) {
                                            $('#file_progress .file_size').html('');
                                            $('#file_progress .file_progressbar').css({ width: '100%' });
                                            $('#file_progress .file_status').html('Success');
                                            clearInterval(upload_html_interval);
                                        }
                                    }
                                }
                                else
                                {
                                    $('#file_progress .file_size').html('');
                                    $('#file_progress .file_progressbar').css({ width: '100%' });
                                    $('#file_progress .file_status').html('Success');
                                    clearInterval(upload_html_interval);
                                }
                            }
                        }
                    });
                }, 300);
                return true;
            });
        });
</script>
</head>
<body>
<div id="upload_container">
    <form id="upload_html_form" target="uploadframe" class="form" action="<% out.write(upload_url); %>" method="post" enctype="multipart/form-data">
    <div class="field_wrapper">
    <input id="upload_file" type="file" accept="video/*" class="required input_file" name="file">
    </div>
    <input id="upload_submit" type="submit" name="submit" class="button large input_submit" value="Upload file">
    <div><iframe id="uploadframe" name="uploadframe" width="0" height="0" frameborder="0" border="0" src="about:blank"></iframe></div>
    </form>
    <div id="swfu_files"></div>
</div>

</body>
</html>

