<?php
/*

Author: Edmond Chuc
Date: 29/03/2018
Description: Preprocesses files for LODE.

*/

if($_FILES['file'])
{
	// directory to upload files to
	$target_dir = "files/";

	// full file path of uploaded file
	$target_file = $target_dir . basename($_FILES["file"]["name"]);

	// temp. name of uploaded file
	$tmp_name = $_FILES["file"]["tmp_name"];

	// get all file names
	$files = glob('files/*');

	// iterate files
	foreach($files as $file)
	{ 
	 	if(is_file($file))
	  	{
	  		if($file != "files/readme.txt")
	  		{
	  			unlink($file); // delete file
	  		}
	    	
		}
	}

	// move file from temp. location to target file directory
	if (move_uploaded_file($tmp_name, $target_file)) 
	{
        // echo "The file ". basename( $_FILES["file"]["name"]). " has been uploaded.";

        // call the LODE service to process the new uploaded file
        $url = "http://localhost:8080/lode/extract?url=http://localhost/files/" . basename($_FILES['file']['name']);
        header("Location: $url");
    } else 
    {
        echo "Sorry, there was an error uploading your file.";
    }
}

	
?>