#!/usr/bin/env python3
"""===============================================================================

        FILE: upload_settings.py

       USAGE: ./upload_settings.py

 DESCRIPTION: 

     OPTIONS: ---
REQUIREMENTS: ---
        BUGS: ---
       NOTES: ---
      AUTHOR: Alex Leontiev (alozz1991@gmail.com)
ORGANIZATION: 
     VERSION: ---
     CREATED: 2020-10-25T19:08:18.584469
    REVISION: ---

==============================================================================="""

import click
import json
from os.path import join
from pymongo import MongoClient


@click.group()
@click.option("--mongopass", envvar="MONGO_PASS")
@click.pass_context
def upload_settings(ctx, **kwargs):
    assert kwargs["mongopass"] is not None
    ctx.ensure_object(dict)
#    for k, v in kwargs.items():
#        ctx.obj[k] = v
    ctx.obj["mongo_client"] = MongoClient(
        f"mongodb://nailbiter:{kwargs['mongopass']}@ds149672.mlab.com:49672/logistics?retryWrites=false")


@upload_settings.command()
@click.pass_context
def upload(ctx):
    with open(".upload_settings.json") as f:
        _upload_settings = json.load(f)
    for r in _upload_settings:
        with open(join("settings", r["filename"])) as f:
            data = json.load(f)
        coll = ctx.obj["mongo_client"]["logistics"][r["collection"]]
        coll.delete_many({})
        coll.insert_many(data)


def _eval(s):
    res = 0
    for a in s.split("+"):
        product = 1
        for m in a.split("*"):
            product *= int(m)
        res += product
    return res    

@upload_settings.command()
@click.argument("name")
@click.argument("cronline")
@click.argument("delaymin")
@click.option("--info", default="")
@click.option("--category", type=click.Choice(["logistics", "social"]))
@click.option("--onFailed", type=click.Choice(["move:FAILED", "move:FAILED2", "move:TODO", "move:todo", "remove"]), default="move:TODO")
@click.pass_context
def add_habit(ctx, category, **kwargs):
    kwargs["delaymin"] = _eval(kwargs["delaymin"])
    assert kwargs["delaymin"] > 0
    obj = {**kwargs, "enabled": True}
    if category is not None:
        obj["category"] = category
    data = []
    with open("settings/habits.json") as f:
        data = json.load(f)
    with open("settings/habits.json", "w") as f:
        data = [*data, obj]
        json.dump(data, f, sort_keys=True, indent=2)


if __name__ == "__main__":
    upload_settings()
