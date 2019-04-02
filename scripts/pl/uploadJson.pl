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


#global var's
my $Testflag = 0;
#procedures
sub my_update_one{
	(my $coll,my @args) = @_;
	printf("update_one(%s,%s)\n",Dumper($args[0],$args[1]));
	$coll->update_one($args[0],$args[1]) unless($Testflag);
}
sub loadJsonFromStdin{
	my $document;
		$document = do { local $/; <> };
	printf(STDERR "doc: %s\n",$document);
	return from_json($document);
}
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
for(qw( dbname colname field flagfile )){
    $args{$_.'=s'} = \$cmdline{$_};
}
$args{'object=s@'} = \$cmdline{object};

GetOptions(%args,);

my $json = loadJsonFromStdin($cmdline{file});

my $client = MongoDB->connect();
my $mongoPassword = $client->ns("admin.passwords")->find_one({key=>"MONGOMLAB"})->{value};
$client = MongoDB->connect(sprintf("mongodb://%s:%s\@ds149672.mlab.com:49672/logistics","nailbiter",$mongoPassword));
my $ns = sprintf("%s.%s",$cmdline{dbname},$cmdline{colname});
my $coll = $client->ns($ns);
if(ref($json) eq 'ARRAY'){
	if( defined $cmdline{field} ) {
		my $field = $cmdline{field};
		for( @$json ) {
			my %record = %$_;
			for( keys %record ) {
				my $key = $_;
				if( $key ne $field ) {
					if(defined($cmdline{object}) && grep( /^$key$/, @{$cmdline{object}} )) {
						my %object = %{$record{$key}};
						for my $kkey (keys %object) {
							my_update_one($coll,
								{$field=>$record{$field}},
								{'$set'=>{sprintf("%s.%s",$key,$kkey)=>$object{$kkey}}},
								{upsert => 1},
							);
						}
					} else {
						my_update_one($coll,
							{$field=>$record{$field}},
							{'$set'=>{$key=>$record{$key}}},
							{upsert => 1},
						);
					}
				}
			}
		}
	} else {
		$coll->drop();
		$coll->insert_many($json);
	}
} elsif(exists $cmdline{field}) {
	for(keys(%$json)){
		my $key = $_;
		my %val = %{$json->{$_}};
		printf("key: %s\nval: %s\n",$key,Dumper(\%val));
		for(keys %val){
			my_update_one($coll,{$cmdline{field}=>$key},{'$set'=>{$_=>$val{$_}}});
		}
	}
} else {
	...
}
if( defined $cmdline{flagfile} ) {
	system(sprintf("touch %s",$cmdline{flagfile}));
}
