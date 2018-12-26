#!/usr/bin/env perl 
#===============================================================================
#
#         FILE: uploadJson.pl
#
#        USAGE: ./uploadJson.pl  
#
#  DESCRIPTION: fill logistics.gym database
#
#      OPTIONS: ---
# REQUIREMENTS: ---
#         BUGS: ---
#        NOTES: ---
#       AUTHOR: YOUR NAME (), 
# ORGANIZATION: 
#      VERSION: 1.0
#      CREATED: 10/24/18 12:04:24
#     REVISION: ---
#===============================================================================

use strict;
use warnings;
use utf8;
use Getopt::Long;
use Data::Dumper;
use MongoDB;
use JSON;


#procedures
sub loadJsonFromFile{
	(my $fn) = @_;
	printf(STDERR "opening file %s\n",$fn);
	my $document;
	my $fh;
	if(open($fh, $fn)){
		$document = do { local $/; <$fh> };
	} else {
		$document ="{}";
	}
	printf(STDERR "doc: %s\n",$document);
	close($fh);
	return from_json($document);
}

#main
my %cmdline;
my %args;
for(qw( file dbname colname )){
    $args{$_.'=s'} = \$cmdline{$_};
}

GetOptions(%args);
$cmdline{dbname} //= 'logistics';

my $json = loadJsonFromFile($cmdline{file});

my $client = MongoDB->connect();
my $mongoPassword = $client->ns("admin.passwords")->find_one({key=>"MONGOMLAB"})->{value};
$client = MongoDB->connect(sprintf("mongodb://%s:%s\@ds149672.mlab.com:49672/logistics","nailbiter",$mongoPassword));
my $ns = sprintf("%s.%s",$cmdline{dbname},$cmdline{colname});
$client->ns($ns)->drop();
if(ref($json) eq 'ARRAY'){
  $client->ns($ns)->insert_many($json);
} else {
  $client->ns($ns)->insert_one($json);
}
