#!/usr/bin/env perl 
#===============================================================================
#
#         FILE: makereport.pl
#
#        USAGE: ./makereport.pl  
#
#  DESCRIPTION: make time report
#
#      OPTIONS: ---
# REQUIREMENTS: ---
#         BUGS: ---
#        NOTES: ---
#       AUTHOR: YOUR NAME (), 
# ORGANIZATION: 
#      VERSION: 1.0
#      CREATED: 01/19/19 21:57:54
#     REVISION: ---
#===============================================================================

use strict;
use warnings;
use utf8;
#use Acme::Magic::Pony;
use Net::OpenSSH;
use Data::Dumper;
use MongoDB;
use Template;
use DateTime;
use FindBin;
use Path::Tiny;


#global const's
my $HOST = 'alumni.cs.nctu.edu.tw';
my $REMOTEPATH = '/u/cs/98/9822058/public_html/forcandice/test.html';
my $LOCALPATH = '/tmp/report.html';
my $DEFNUMOFTIMERECORDS = 15;
my $TIMEDB = {
	dbname => 'logistics',
	colname => 'alex.time',
};
my $CSS = <<'END_BLURB';
table, th, td {
    border: 1px solid black;
}
body {
	background: #293f50;
	color: #880;
	font: 300 16px/22px "Lato", "Open Sans", "Helvetica Neue", Helvetica, Arial, sans-serif;
}
END_BLURB
my $MONGOMLAB = "mongodb://%s:%s\@ds149672.mlab.com:49672/logistics";
my $HTMLREPORTTEMPLATE = <<'END_BLURB';
<html>
	<head>
		<meta charset="UTF-8">
		<title>唐琳，您好！</title>
		<link rel="stylesheet" href="style.css"></link>
	</head>
	<body>
		<h1>唐琳，您好！</h1>
		您現在在看這個網頁，應該是擔心，Alex有沒有在看書，不理您。<br>
		請您看一下下面的表。這裡是寫的Alex最近在做什麼（最上面的是最近的，最下面的是最久的）。
		如果您下面看到了太多<b>useless</b>（什麼都不做）或是<b>sleeping</b>（睡覺），那他應該在看書的可能性是比較高。
		<br><br>
		<table align="center">
			[% FOREACH record IN CONTENT %]
				<tr>
					[% FOREACH item IN record %]
					<td>[% item %]</td> 
					[% END %]
				</tr>
			[% END %]
		</table>
		<div align="right">
		<h1>祝好，Alex 的女朋友。</h1>
		<h1>PS. 我用繁體字寫，因為我還是是臺灣的。</h1>
		</div>
</body>
</html>
END_BLURB
#procedures
sub getPassword{
	my $mongoPassword = path("$FindBin::Bin/../../secret.txt")->slurp_utf8;
	chomp $mongoPassword;
	my $mongoClient = MongoDB->connect(sprintf($MONGOMLAB,"nailbiter",$mongoPassword));
	return $mongoClient->ns("logistics._keyring")->find_one()->{alumnipass};
}
sub uploadFile{
	my $ssh = Net::OpenSSH->new($HOST, user => 'inp9822058', password => getPassword());
	$ssh->scp_put($LOCALPATH, $REMOTEPATH)
		or die "scp failed: " . $ssh->error;
}
sub createFile{
	my $filename = $LOCALPATH;
	open(my $fh, '>', $filename) or die "Could not open file '$filename' $!";
	my $client = MongoDB->connect();
	my $mongoPassword = path("$FindBin::Bin/../../secret.txt")->slurp_utf8;
	chomp $mongoPassword;
	$client = MongoDB->connect(sprintf($MONGOMLAB,"nailbiter",$mongoPassword));
	my $coll = $client->get_database($$TIMEDB{dbname})->get_collection($$TIMEDB{colname})
		->find()->sort({date=>-1})->limit($DEFNUMOFTIMERECORDS);
	my @res;
	while(my $next = $coll->next) {
		my $datetime = $next->{date}->as_datetime;
		$datetime->set_time_zone( 'Asia/Tokyo' );
		my $date = $datetime->stringify;
#		<td>2019-01-19T22:30:05.957Z</td> 
		$date =~ /([0-9]{4})-([0-9]{2})-([0-9]{2})T([0-9]{2}):([0-9]{2}):([0-9]{2})/;
		push(@res,[
#				$date,
				sprintf("%s-%s-%s",$1,$2,$3),
				sprintf("%s:%s",$4,$5),
				$$next{category},
			]);
	}
	my $tt = Template->new({
		INCLUDE_PATH => '.',
	}) || die "$Template::ERROR\n";
	$tt->process(\$HTMLREPORTTEMPLATE, {
			CONTENT => \@res,
		},$fh)|| die $tt->error(), "\n";
	close($fh);
}

#main
createFile();
uploadFile();
#*/5 * * * * /home/nailbiter/for/forperl/makereport.pl
