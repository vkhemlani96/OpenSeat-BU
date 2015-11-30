<?php

$params = $_GET['params'];
$classes = explode(";",$params);

for ($i = 0; $i<count($classes); $i++) {
	$data = explode(",",$classes[$i]);
	
	$url = "http://www.bu.edu/link/bin/uiscgi_studentlink.pl/1431308709?ModuleName=univschr.pl&SearchOptionDesc=Class+Number&SearchOptionCd=S&KeySem=20163&ViewSem=Fall+2015&College=".$data[0]."&Dept=".$data[1]."&Course=".$data[2]."&Section=".$data[3];
	$ch = curl_init();
	$timeout = 10;
	curl_setopt($ch, CURLOPT_URL, $url);
	curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);
	curl_setopt($ch, CURLOPT_CONNECTTIMEOUT, $timeout);
	$html = curl_exec($ch);
	curl_close($ch);

	# Create a DOM parser object
	$dom = new DOMDocument();

	# Parse the HTML from Google.
	# The @ before the method call suppresses any warnings that
	# loadHTML might throw because of invalid HTML in the page.
	@$dom->loadHTML($html);
	
	# Iterate over all the <table> tags
	foreach($dom->getElementsByTagName('td') as $link) {
		# Show the <table href>
		$cell_content = $link->textContent;
		
		$found = true;
		foreach ($data as $text) {
			if (strpos($cell_content, $text) === false) {
				$found = false;
			}
		}
		
		if ($found) {
			$row = $link->parentNode;
			echo $classes[$i] . "-" . $row->childNodes->item(12)->textContent . "-";
			if (strpos($row->textContent, "Closed") !== false) {
				echo "C";
			} elseif (strpos($row->textContent, "Full") !== false) {
				echo "F";
			} elseif ($row->childNodes->item(25)->textContent == "") {
				echo "O";
			} else {
				echo $row->textContent;
			}
			echo "<br/>";
		}
	}
	
}
	
?>