<?php
/*

Author: Edmond Chuc
Date: 29/03/2018
Description: Forwards the URL ontology to LODE.

*/

	// if valid URL,
	if( $_POST["url"] ) 
	{
		// call the LODE service
		$url = $_POST["url"];
		header("Location: $url");

		exit();
	}
?>