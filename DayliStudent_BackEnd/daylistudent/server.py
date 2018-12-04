import json
import logging.config
import os
import sqlite3
from ast import literal_eval

from flask import Flask, request, g
from flask_api import status

from daylistudent.models.group import Group
from daylistudent.models.note import Note
from daylistudent.utils.logger import setup_logging

logger = logging.getLogger(__name__)

# Endpoints ------------------------------------------------------------------------------------------------------------

app = Flask(__name__)
app.config['DEBUG'] = True


@app.route("/groups", methods=['GET'])
def getGroups():
    """ Query db to get all Groups """
    logger.info('getGroup()')
    result_groups = execute_select_query('SELECT * FROM Groups;')
    groups = []
    for result in result_groups:
        groups.append(Group.toJson(result[0], result[1]))
    return json.dumps(groups), status.HTTP_200_OK


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
        logger.info('Group created with name: {}'.format(group_name))
        group_id = execute_select_query('SELECT id FROM Groups WHERE name={};'.format(group_name))
        logger.info('Group created with id: {}'.format(group_id))
        return json.dumps(group_id), status.HTTP_200_OK
    else:
        return status.HTTP_404_NOT_FOUND


@app.route("/group/<str:group_id>", methods=['DELETE'])
def deleteGroup(group_id):
    """ Query db to delete a certain Group with group_id """
    logger.info('deleteGroup(group_id: {})'.format(group_id))
    notes_count = execute_select_query('SELECT COUNT(id) FROM Notes WHERE groupId={};'.format(group_id))
    logger.info('notes_count: {}'.format(notes_count))
    if notes_count == 0:
        rows_changed = execute_delete_query('DELETE FROM Groups WHERE id={};'.format(group_id))
        return json.dumps(rows_changed), status.HTTP_200_OK
    else:
        return status.HTTP_405_METHOD_NOT_ALLOWED


@app.route('/notes/<int:group_id>', methods=['GET'])
def getNotes(group_id):
    """ Query db to get all notes from a certain Group with group_id """
    logger.info('getNotes(group_id:{})'.format(group_id))
    result_notes = execute_select_query('SELECT * FROM Notes WHERE groupId={};'.format(group_id))
    notes = []
    for result in result_notes:
        notes.append(Note.toJson(result[0], result[1], result[2], result[3], result[4]))
    return json.dumps(notes), status.HTTP_200_OK


@app.route('/note', methods=['POST'])
def postNote():
    """ Query db to create a new Note from received JSON data """
    logger.info('postNote()')
    if not request.data:
        return status.HTTP_400_BAD_REQUEST

    logger.info('receivedData: {}'.format(request.data))
    logger.info('receivedDataType: {}'.format(type(request.data)))

    note = json.loads(literal_eval(request.data.decode("utf8")))
    logger.info('deserializedNote: {}'.format(str(note)))
    logger.info('deserializedNote type: {}'.format(type(note)))

    if 'groupId' not in note or 'username' not in note or 'title' not in note or 'body' not in note:
        return status.HTTP_400_BAD_REQUEST

    group_id = int(note['groupId'])
    username = str(note['username'])
    title = str(note['title'])
    body = str(note['body'])

    result = execute_insert_query(
        'INSERT INTO Notes (groupId, username, title, body) VALUES (\'{}\', \'{}\', \'{}\', \'{}\');'.format(group_id,
                                                                                                             username,
                                                                                                             title,
                                                                                                             body))
    logger.info('result: {}'.format(result))
    return json.dumps(result), status.HTTP_200_OK


@app.route('/note/<int:note_id>', methods=['POST'])
def updateNote(note_id):
    """ Query db to update a certain Note from received JSON data with a certain note_id """
    logger.info('updateNote(note_id: {})'.format(note_id))
    if not request.data:
        return status.HTTP_400_BAD_REQUEST

    logger.info('receivedData: {}'.format(request.data))

    note = json.loads(literal_eval(request.data.decode("utf8")))
    logger.info('deserializedNote: {}'.format(str(note)))
    logger.info('deserializedNote type: {}'.format(type(note)))

    if 'username' not in note or 'title' not in note or 'body' not in note:
        return status.HTTP_400_BAD_REQUEST

    username = str(note['username'])
    title = str(note['title'])
    body = str(note['body'])

    result = execute_update_query('UPDATE Notes SET username=\'{}\', title=\'{}\', body=\'{}\' WHERE id=\'{}\';'
                                  .format(username, title, body, note_id))
    return json.dumps(result), status.HTTP_200_OK


@app.route("/note/<int:note_id>", methods=['DELETE'])
def deleteNote(note_id):
    """ Query db to delete a certain Group with group_id """
    logger.info('deleteNote(note_id:{})'.format(note_id))
    result = execute_delete_query('DELETE FROM Notes WHERE id={};'.format(note_id))
    logger.info('result: {}'.format(result))
    return json.dumps(result), status.HTTP_200_OK


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
    db_conn.commit()
    return curr.rowcount


def run():
    setup_logging()
    logger.info('Dayli Student started')
    app.run(host="0.0.0.0", port=8080)


if __name__ == '__main__':
    run()
