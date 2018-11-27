import json
import logging.config
import os
import sqlite3

from flask import Flask, request, g
from flask_api import status

from daylistudent.utils.logger import setup_logging

logger = logging.getLogger(__name__)

# Endpoints ------------------------------------------------------------------------------------------------------------

app = Flask(__name__)
app.config['DEBUG'] = True
app.config['PORT'] = 5001


@app.route("/groups", methods=['GET'])
def getGroups():
    """ Query db to get all Groups """
    logger.info('getGroup()')
    result = execute_select_query('SELECT * FROM Groups;')
    return json.dumps(result), status.HTTP_200_OK


@app.route('/group/<int:group_id>', methods=['GET'])
def getGroup(group_id):
    """ Query db to get Group with group_id """
    logger.info('getGroup(group_id:{})'.format(group_id))
    result = execute_select_query('SELECT * FROM Groups WHERE id={};'.format(group_id))
    return json.dumps(result), status.HTTP_200_OK


@app.route('/group', methods=['POST'])
def postGroup():
    """ Query db to create a new Group with group_name """
    logger.info('postGroup()')
    group_name = request.args.get('group_name')
    rows_changed = execute_insert_query('INSERT INTO Groups (name) VALUES (\'{}\');'.format(group_name))
    if rows_changed > 0:
        return json.dumps(rows_changed), status.HTTP_200_OK
    else:
        return status.HTTP_404_NOT_FOUND


@app.route("/group/<int:group_id>", methods=['DELETE'])
def deleteGroup(group_id):
    """ Query db to delete a certain Group with group_id """
    logger.info('deleteGroup(group_id:{})'.format(group_id))
    rows_changed = execute_delete_query('DELETE FROM Groups WHERE id={}'.format(group_id))
    if rows_changed > 0:
        return json.dumps(rows_changed), status.HTTP_200_OK
    else:
        return status.HTTP_404_NOT_FOUND


@app.route('/notes/<int:group_id>', methods=['GET'])
def getNotes(group_id):
    """ Query db to get all notes from a certain Group with group_id """
    logger.info('getNotes(group_id:{})'.format(group_id))
    result = execute_select_query('SELECT * FROM Notes WHERE groupId={};'.format(group_id))
    return json.dumps(result), status.HTTP_200_OK


@app.route('/note', methods=['POST'])
def postNote():
    """ Query db to create a new Note from received JSON data """
    print(request.is_json)
    content = request.get_json()
    print(content)
    return 200


# TODO: deleteNote && updateNote

@app.route("/note/<int:note_id>")
def deleteNote(note_id):
    """ Query db to delete a certain Group with group_id """
    logger.info('deleteNote(note_id:{})'.format(note_id))
    return 200


DATABASE = "./database.db"

if not os.path.exists(DATABASE):
    db_schema_lines = open('db/db_schema.sql', 'r').readlines()
    logger.info(db_schema_lines)
    conn = sqlite3.connect(DATABASE)
    cur = conn.cursor()
    for query_line in db_schema_lines:
        cur.execute(query_line)
    conn.commit()
    conn.close()


# Database -------------------------------------------------------------------------------------------------------------

def get_db():
    db = getattr(g, '_database', None)
    if db is None:
        db = g._database = sqlite3.connect(DATABASE)
    return db


@app.teardown_appcontext
def close_connection(exception):
    logger.error(exception)
    db = getattr(g, '_database', None)
    if db is not None:
        db.close()


def execute_select_query(select_query):
    db_conn = get_db()
    curr = db_conn.cursor()
    curr.execute(select_query)
    rows = curr.fetchall()
    return rows


def execute_insert_query(insert_query):
    db_conn = get_db()
    curr = db_conn.cursor()
    curr.execute(insert_query)
    db_conn.commit()
    return curr.rowcount


def execute_update_query(update_query):
    db_conn = get_db()
    curr = db_conn.cursor()
    curr.execute(update_query)
    db_conn.commit()
    return curr.rowcount


def execute_delete_query(delete_query):
    db_conn = get_db()
    curr = db_conn.cursor()
    curr.execute(delete_query)
    return curr.rowcount


def run():
    setup_logging()
    logger.info('Dayli Student started')
    app.run(host="0.0.0.0")


if __name__ == '__main__':
    run()
